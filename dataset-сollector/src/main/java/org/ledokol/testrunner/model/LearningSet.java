package org.ledokol.testrunner.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document("learningSet2")
public class LearningSet {
    @Id
    ObjectId id;
    int vuser;
    double meanCPU;
    double maxCPU;
    double meanRAM;
    double maxRAM;
    int pacing;
    int requestSize;
    int steps;
    Date startDate;
    Date endDate;
    String generatorInstanceId;
    Boolean is_done;
    Boolean redone;

    public LearningSet(ParameterSet parameterSet){
        this.id = parameterSet.getId();
        this.vuser = parameterSet.getVuser();
        this.pacing = parameterSet.getPacing();
        this.steps = parameterSet.getSteps();
        this.requestSize = parameterSet.getRequestSize();
        this.startDate = parameterSet.getStartDate();
        this.endDate = parameterSet.getEndDate();
        this.generatorInstanceId = parameterSet.getGeneratorInstanceId();
        this.maxCPU = parameterSet.getMaxCPU();
        this.meanCPU = parameterSet.getMeanCPU();
        this.maxRAM = parameterSet.getMaxRAM();
        this.meanRAM = parameterSet.getMeanRAM();
        this.is_done = parameterSet.getIs_done();
    }

    public LearningSet(ObjectId id, int vuser, int pacing, int requestSize, int steps,
                       boolean isDone, boolean redone, Date startDate, Date endDate, String generatorId) {
        this.id = id;
        this.vuser = vuser;
        this.pacing = pacing;
        this.requestSize = requestSize;
        this.steps = steps;
        this.is_done = isDone;
        this.redone = redone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.generatorInstanceId = generatorId;
    }
    public LearningSet(int vuser, int pacing, int requestSize, int steps, boolean isDone, boolean redone) {
        this.vuser = vuser;
        this.pacing = pacing;
        this.requestSize = requestSize;
        this.steps = steps;
        this.is_done = isDone;
        this.redone = redone;
    }

    public LearningSet(int vuser, int pacing, int requestSize, int steps, boolean isDone, boolean redone, Date startDate, Date endDate) {
        this.vuser = vuser;
        this.pacing = pacing;
        this.requestSize = requestSize;
        this.steps = steps;
        this.is_done = isDone;
        this.redone = redone;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return  "pacing: " + this.pacing +
                "\nsteps: " + this.steps +
                "\nbodySize: " + this.requestSize +
                "\nvusers: " + this.vuser;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getVuser() {
        return vuser;
    }

    public void setVuser(int vuser) {
        this.vuser = vuser;
    }

    public double getMeanCPU() {
        return meanCPU;
    }

    public void setMeanCPU(double meanCPU) {
        this.meanCPU = meanCPU;
    }

    public double getMaxCPU() {
        return maxCPU;
    }

    public void setMaxCPU(double maxCPU) {
        this.maxCPU = maxCPU;
    }

    public double getMeanRAM() {
        return meanRAM;
    }

    public void setMeanRAM(double meanRAM) {
        this.meanRAM = meanRAM;
    }

    public double getMaxRAM() {
        return maxRAM;
    }

    public void setMaxRAM(double maxRAM) {
        this.maxRAM = maxRAM;
    }

    public int getPacing() {
        return pacing;
    }

    public void setPacing(int pacing) {
        this.pacing = pacing;
    }

    public int getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getGeneratorInstanceId() {
        return generatorInstanceId;
    }

    public void setGeneratorInstanceId(String generatorInstanceId) {
        this.generatorInstanceId = generatorInstanceId;
    }

    public Boolean getIs_done() {
        return is_done;
    }

    public void setIs_done(Boolean is_done) {
        this.is_done = is_done;
    }

    public Boolean getRedone() {
        return redone;
    }

    public void setRedone(Boolean redone) {
        this.redone = redone;
    }
}
