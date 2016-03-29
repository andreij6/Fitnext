package com.creativejones.andre.fitnext.data;

import java.util.ArrayList;
import java.util.List;

public class Workout {
    List<String> mExercises = new ArrayList<>();
    int mExerciseIndex = 0;

    public Workout(){
        basicExercise();
    }

    public void basicExercise() {
        mExercises.add("Push Ups 4x20");
        mExercises.add("Set Ups 5x20");
        mExercises.add("V Ups 5x20");
        mExercises.add("Bench 5x20");
        mExercises.add("Curls 5x20");
        mExercises.add("Squats 5x20");
    }

    public String nextExercise() {
        mExerciseIndex++;

        if(mExerciseIndex >= mExercises.size()){
            mExerciseIndex = 0;
        }

        return mExercises.get(mExerciseIndex);

    }
}
