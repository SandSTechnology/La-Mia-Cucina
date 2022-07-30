package com.example.lamiacucina.model;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Ingredient implements Serializable {
    String ID;
    String IngredientName;
    String IngredientQuantity;
    String IngredientUnit;
    String IngredientThresholdValue;

    public Ingredient() {
    }

    public Ingredient(String ID, String ingredientName, String ingredientQuantity, String ingredientUnit, String ingredientThresholdValue) {
        this.ID = ID;
        IngredientName = ingredientName;
        IngredientQuantity = ingredientQuantity;
        IngredientUnit = ingredientUnit;
        IngredientThresholdValue = ingredientThresholdValue;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getIngredientName() {
        return IngredientName;
    }

    public void setIngredientName(String ingredientName) {
        IngredientName = ingredientName;
    }

    public String getIngredientQuantity() {
        return IngredientQuantity;
    }

    public void setIngredientQuantity(String ingredientQuantity) {
        IngredientQuantity = ingredientQuantity;
    }

    public String getIngredientUnit() {
        return IngredientUnit;
    }

    public void setIngredientUnit(String ingredientUnit) {
        IngredientUnit = ingredientUnit;
    }

    public String getIngredientThresholdValue() {
        return IngredientThresholdValue;
    }

    public void setIngredientThresholdValue(String ingredientThresholdValue) {
        IngredientThresholdValue = ingredientThresholdValue;
    }

    @NonNull
    @Override
    public String toString() {
        return IngredientName + IngredientQuantity + IngredientUnit;
    }
}
