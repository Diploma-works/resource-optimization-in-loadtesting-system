class TestDistributor:
    def __init__(self, prometheus_service, predictor_service, api_service, generator_service):
        self.prometheus_service = prometheus_service
        self.predictor_service = predictor_service
        self.api_service = api_service
        self.generator_service = generator_service

    def process_test(self, test: Test):
        try:
            generators_list_sorted = self.generator_service.get_generators_sorted_by_load()
            return self.distribute_test(generators_list_sorted, test)
        except Exception as e:
            raise RuntimeError(f"Failed to distribute test: {str(e)}")

    def get_test_params_as_ndarray(self, test: Test):
        pacing = test.scenario.pacing
        steps = len(test.scenario.script.steps)
        body_size = sum(getBodySize(step.body) for step in test.scenario.script.steps)
        vusers_amount = test.total_user_count

        return pacing, steps, body_size, vusers_amount

    def distribute_test(self, sorted_generators, test):
        result_test_plan = []
        remaining_vusers = test.total_user_count

        pacing, steps, body_size, vusers_amount = self.get_test_params_as_ndarray(test)

        for generator_id in sorted_generators:
            if remaining_vusers <= 0:
                break

            predicted_cpu_for_all = self.predictor_service.predict_cpu_utilization(pacing, steps, body_size, remaining_vusers)
            predicted_ram_for_all = self.predictor_service.predict_ram_utilization(pacing, steps, body_size, remaining_vusers)

            cpu_left = self.generator_service.get_cpu_amount_left_on_generator(self.generator_service.get_max_cpu_utilization_during_test(generator_id))
            ram_left = self.generator_service.get_ram_amount_left_on_generator(self.generator_service.get_max_ram_utilization_during_test(generator_id))

            if predicted_cpu_for_all <= cpu_left and predicted_ram_for_all <= ram_left:
                rog = RunOnGenerator(generator_id, test.scenario, remaining_vusers)
                result_test_plan.append(rog)
                remaining_vusers = 0
                break
            else:
                vusers_to_allocate = self.find_optimal_vusers(cpu_left, ram_left, remaining_vusers)
                if vusers_to_allocate > 0:
                    rog = RunOnGenerator(generator_id, test.scenario, vusers_to_allocate)
                    result_test_plan.append(rog)
                    remaining_vusers -= vusers_to_allocate

        return result_test_plan

    def find_optimal_vusers(self, free_cpu, free_ram, max_vusers):
        low, high = 0, max_vusers
        best_guess = 0

        while low <= high:
            mid = (low + high) // 2
            pacing, steps, body_size, vusers_count = self.get_test_params_as_ndarray(mid)
            predicted_cpu = self.predictor_service.predict_cpu_utilization(pacing, steps, body_size, mid)
            predicted_ram = self.predictor_service.predict_ram_utilization(pacing, steps, body_size, mid)

            if predicted_cpu <= free_cpu and predicted_ram <= free_ram:
                best_guess = mid
                low = mid + 1
            else:
                high = mid - 1

        return best_guess


    def getBodySize(body):
        size_in_bytes = len(body.encode('utf-8'))
        return size_in_bytes / 1024
