package com.humaralabs.fieldrun.datastructure;
public class Review {

    public String questionId;
    public String question;
    public String answerType;


    public Review(String questionId,String question,String answerType)  {
        this.questionId = questionId;
        this.question = question;
        this.answerType = answerType;

    }
}
