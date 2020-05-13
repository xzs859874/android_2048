package com.a2048;

public class RankBean {

    private Integer id;
    private Integer score;
    private String name;
    private Integer num;


    public RankBean(Integer id,String name,Integer score,Integer num){

        this.score = score;
        this.id = id;
        this.name = name;
        this.num = num;
    }
    public RankBean(String name,Integer score){

        this.score = score;
        this.name = name;

    }
    public String getName(){
        return name;
    }
    public Integer getId(){
        return id;
    }
    public Integer getScore(){
        return score;
    }
    public Integer getNum(){return num;}


}
