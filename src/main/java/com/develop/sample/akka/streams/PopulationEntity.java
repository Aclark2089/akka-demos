package com.develop.sample.akka.streams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PopulationEntity {

    private String countryCode;
    private String countryName;
    private double value;
    private int year;

    @JsonProperty("Country Code")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonProperty("Country Name")
    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @JsonProperty("Value")
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @JsonProperty("Year")
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "PopulationEntity{" +
                "countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", value=" + value +
                ", year=" + year +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopulationEntity)) return false;
        PopulationEntity that = (PopulationEntity) o;
        return Double.compare(that.value, value) == 0 &&
                year == that.year &&
                Objects.equals(countryCode, that.countryCode) &&
                Objects.equals(countryName, that.countryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, countryName, value, year);
    }

}
