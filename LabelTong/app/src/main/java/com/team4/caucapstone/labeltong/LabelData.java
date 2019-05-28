package com.team4.caucapstone.labeltong;

public class LabelData {
    private String ID;
    private String CreatedAt;
    private String UpdatedAt;
    private String DeletedAt;
    private String DatasetId;
    private String IsFake;
    private String RequiredNumAnswer;
    private String DataPath;
    private String AnswerType;
    private String Question;
    private String TagList;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

    public String getDeletedAt() {
        return DeletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        DeletedAt = deletedAt;
    }

    public String getDatasetId() {
        return DatasetId;
    }

    public void setDatasetId(String datasetId) {
        DatasetId = datasetId;
    }

    public String getIsFake() {
        return IsFake;
    }

    public void setIsFake(String isFake) {
        IsFake = isFake;
    }

    public String getRequiredNumAnswer() {
        return RequiredNumAnswer;
    }

    public void setRequiredNumAnswer(String requiredNumAnswer) {
        RequiredNumAnswer = requiredNumAnswer;
    }

    public String getDataPath() {
        return DataPath;
    }

    public void setDataPath(String dataPath) {
        DataPath = dataPath;
    }

    public String getAnswerType() {
        return AnswerType;
    }

    public void setAnswerType(String answerType) {
        AnswerType = answerType;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getTagList() {
        return TagList;
    }

    public void setTagList(String tagList) {
        TagList = tagList;
    }
}
