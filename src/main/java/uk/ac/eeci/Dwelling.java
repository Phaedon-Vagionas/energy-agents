package uk.ac.eeci;

import java.util.function.Function;

public class Dwelling {

    private double currentTemperature;
    private final double heatMassCapacity;
    private final double heatTransmission;
    private final double maximumCoolingPower;
    private final double maximumHeatingPower;
    private final double conditionedFloorArea;
    private final int timeStepSize;

    /**
     * A simple energy model of a dwelling.
     *
     * Consisting of one thermal capacity and one resistance, this model is derived from the
     * hourly dynamic model of the ISO 13790. It models heating and cooling energy demand only.
     *
     *
     * @param heatMassCapacity capacity of the dwelling's heat mass [J/K]
     * @param heatTransmission heat transmission to the outside [W/K]
     * @param maximumCoolingPower [W] (<= 0)
     * @param maximumHeatingPower [W] (>= 0)
     * @param initialDwellingTemperature dwelling temperature at start time [℃]
     * @param conditionedFloorArea [m**2]
     * @param timeStepSize [s]
     */
    public Dwelling(double heatMassCapacity, double heatTransmission, double maximumCoolingPower,
                    double maximumHeatingPower, double initialDwellingTemperature,
                    double conditionedFloorArea, int timeStepSize) {
        assert maximumCoolingPower <= 0;
        assert maximumHeatingPower >= 0;
        this.currentTemperature = initialDwellingTemperature;
        this.heatMassCapacity = heatMassCapacity;
        this.heatTransmission = heatTransmission;
        this.maximumCoolingPower = maximumCoolingPower;
        this.maximumHeatingPower = maximumHeatingPower;
        this.conditionedFloorArea = conditionedFloorArea;
        this.timeStepSize = timeStepSize;
    }

    /**
     * Performs dwelling simulation for the next time step.
     *
     * @param outsideTemperature [℃]
     * @param heatingSetPoint heating setpoint of the HVAC system [℃]
     * @param coolingSetPoint cooling setpoint of the HVAC system [℃]
     */
    public void step(double outsideTemperature, double heatingSetPoint, double coolingSetPoint) {
        Function<Double, Double> nextTemperature = thermalPower ->
                this.nextTemperature(outsideTemperature, thermalPower);
        double nextTemperatureNoPower = nextTemperature.apply(0.0);
        if (nextTemperatureNoPower >= heatingSetPoint && nextTemperatureNoPower <= coolingSetPoint) {
            this.currentTemperature = nextTemperatureNoPower;
        }
        else {
            double setPoint;
            double maxPower;
            if (nextTemperatureNoPower < heatingSetPoint) {
                setPoint = heatingSetPoint;
                maxPower = this.maximumHeatingPower;
            }
            else {
                setPoint = coolingSetPoint;
                maxPower = this.maximumCoolingPower;
            }
            double tenWattPowerSquareMeterPower = 10 * this.conditionedFloorArea;
            double nextTemperaturePower10 = nextTemperature.apply(tenWattPowerSquareMeterPower);
            double unrestrictedPower = (tenWattPowerSquareMeterPower *
                    (setPoint - nextTemperatureNoPower) /
                    (nextTemperaturePower10 - nextTemperatureNoPower));
            double thermalPower;
            if (Math.abs(unrestrictedPower) <= Math.abs(maxPower)) {
                thermalPower = unrestrictedPower;
            }
            else {
                thermalPower = maxPower;
            }
            this.currentTemperature = nextTemperature.apply(thermalPower);
        }
    }

    public double getTemperature() {
        return this.currentTemperature;
    }

    private double nextTemperature(double outsideTemperature, double thermalPower) {
        double dt_by_cm = this.timeStepSize / this.heatMassCapacity;
        return (this.currentTemperature * (1 - dt_by_cm * this.heatTransmission) +
                dt_by_cm * (thermalPower + this.heatTransmission * outsideTemperature));

    }


}