package com.example.lamiacucina.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Meal implements Serializable {
    String ID;
    String MenuName;
    String DurationOfMeal;
    String MealTime;
    String MenuType;
    String Serving;
    String Recipe1ImageUrl;
    ArrayList<String> RecipesID = new ArrayList<>();

    public Meal() {
    }

    public Meal(String ID, String menuName, String durationOfMeal, String mealTime, String menuType, String serving, ArrayList<String> recipesID,String url) {
        this.ID = ID;
        MenuName = menuName;
        DurationOfMeal = durationOfMeal;
        MealTime = mealTime;
        MenuType = menuType;
        Serving = serving;
        RecipesID = recipesID;
        Recipe1ImageUrl = url;
    }

    public String getRecipe1ImageUrl() {
        return Recipe1ImageUrl;
    }

    public void setRecipe1ImageUrl(String recipe1ImageUrl) {
        Recipe1ImageUrl = recipe1ImageUrl;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMenuName() {
        return MenuName;
    }

    public void setMenuName(String menuName) {
        MenuName = menuName;
    }

    public String getDurationOfMeal() {
        return DurationOfMeal;
    }

    public void setDurationOfMeal(String durationOfMeal) {
        DurationOfMeal = durationOfMeal;
    }

    public String getMealTime() {
        return MealTime;
    }

    public void setMealTime(String mealTime) {
        MealTime = mealTime;
    }

    public String getMenuType() {
        return MenuType;
    }

    public void setMenuType(String menuType) {
        MenuType = menuType;
    }

    public String getServing() {
        return Serving;
    }

    public void setServing(String serving) {
        Serving = serving;
    }

    public ArrayList<String> getRecipesID() {
        return RecipesID;
    }

    public void setRecipesID(ArrayList<String> recipesID) {
        RecipesID = recipesID;
    }
}
