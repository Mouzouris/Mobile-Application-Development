package com.example.comp6239.Model;
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

public class Lecture {

    private String student_id;
    private String tutor_id;
    private String subject_id;
    private String approved;
    private String date;
    private String time;

    public Lecture(){}
    public Lecture(String student_id, String tutor_id, String subject_id,String approved,String date,String time) {
        this.student_id = student_id;
        this.tutor_id = tutor_id;
        this.subject_id = subject_id;
        this.approved=approved;
        this.date=date;
        this.time=time;
    }

    public String getStudent_id() {return student_id;}
    public String getTutor_id() {return tutor_id;}
    public String getSubject_id() {return subject_id;}
    public String getApproved() {return approved;}
    public String getDate() {return date;}
    public String getTime() {return time;}
    public void setStudent_id(String student_id) {this.student_id = student_id;}
    public void setTutor_id(String tutor_id) {this.tutor_id = tutor_id;}
    public void setSubject_id(String subject_id) {this.subject_id = subject_id;}
    public void setApproved(String approved) {this.approved = approved;}
    public void setDate(String date) {this.date = date;}
    public void setTime(String time) {this.time = time;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("student_id", student_id);
        result.put("tutor_id",tutor_id);
        result.put("subject_id",subject_id);
        result.put("approved",approved);
        result.put("date",date);
        result.put("time",time);
        return result;
    }

    @Override
    public String toString() {
        return "Lecture:" +
                "student_id='" + student_id + '\'' +
                ", tutor_id='" + tutor_id + '\'' +
                ", subject_id='" + subject_id + '\'' +
                "approved="+approved+'\''+"date="+date
                +'\''+"time="+time+'\''+'}';
    }
}
