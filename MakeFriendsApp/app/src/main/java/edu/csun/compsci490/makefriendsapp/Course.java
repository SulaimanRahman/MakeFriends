package edu.csun.compsci490.makefriendsapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class Course {
    private String sectionNumber;
    private String course;
    private String courseNumber;

    public Course(){
        this.sectionNumber = "";
        this.course = "";
        this.courseNumber = "";
    }

    public Course(String sectionNumber, String course, String courseNumber) {
        this.sectionNumber = sectionNumber;
        this.course = course;
        this.courseNumber = courseNumber;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public boolean hasData(){
        if(!this.getSectionNumber().equals("") && !this.getCourseNumber().equals("") && !this.getCourse().equals("")){
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course1 = (Course) o;
        return Objects.equals(sectionNumber, course1.sectionNumber) &&
                Objects.equals(course, course1.course) &&
                Objects.equals(courseNumber, course1.courseNumber);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(sectionNumber, course, courseNumber);
    }
}
