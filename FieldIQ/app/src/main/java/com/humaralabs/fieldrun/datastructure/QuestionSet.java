package com.humaralabs.fieldrun.datastructure;


public class QuestionSet {

    public String questionId;
    public String platformId;
    public String itemCategory;
    public String question;
    public String answerType;
    public String noEffect;


    public QuestionSet(String questionId,String platformId,String question,String answerType,String itemCategory,String noEffect)  {
        this.questionId = questionId;
        this.platformId = platformId;
        this.itemCategory = itemCategory;
        this.question = question;
        this.answerType = answerType;
        this.noEffect = noEffect;
    }
}
