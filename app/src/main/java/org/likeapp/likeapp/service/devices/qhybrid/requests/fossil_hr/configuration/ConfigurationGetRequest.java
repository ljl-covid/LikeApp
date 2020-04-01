package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.configuration;

import org.likeapp.likeapp.deviceevents.GBDeviceEventBatteryInfo;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.model.BatteryState;
import org.likeapp.likeapp.model.GenericItem;
import org.likeapp.likeapp.service.devices.qhybrid.QHybridSupport;
import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil.configuration.ConfigurationPutRequest;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.file.FileEncryptedLookupAndGetRequest;
import org.likeapp.likeapp.util.GB;

public class ConfigurationGetRequest extends FileEncryptedLookupAndGetRequest
{
    public ConfigurationGetRequest(FossilHRWatchAdapter adapter) {
        super((byte) 0x08, adapter);
    }

    @Override
    public void handleFileData(byte[] fileData) {
        byte[] data = new byte[fileData.length - 12 - 4];

        System.arraycopy(fileData, 12, data, 0, data.length);
        log("config file: " + getAdapter().arrayToString(fileData));
        log("config file: " + getAdapter().arrayToString(data));

        GBDevice device = getAdapter().getDeviceSupport().getDevice();

        ConfigurationPutRequest.ConfigItem[] items = ConfigurationPutRequest.parsePayload(data);

        for(ConfigurationPutRequest.ConfigItem item : items){
            if(item instanceof ConfigurationPutRequest.VibrationStrengthConfigItem){
                device.addDeviceInfo(new GenericItem (QHybridSupport.ITEM_VIBRATION_STRENGTH, String.valueOf(((ConfigurationPutRequest.VibrationStrengthConfigItem) item).getValue())));
            }else if(item instanceof ConfigurationPutRequest.DailyStepGoalConfigItem){
                device.addDeviceInfo(new GenericItem(QHybridSupport.ITEM_STEP_GOAL, String.valueOf(((ConfigurationPutRequest.DailyStepGoalConfigItem) item).getValue())));
            }else if(item instanceof ConfigurationPutRequest.CurrentStepCountConfigItem){
                device.addDeviceInfo(new GenericItem(QHybridSupport.ITEM_STEP_COUNT, String.valueOf(((ConfigurationPutRequest.CurrentStepCountConfigItem) item).getValue())));
            }else if(item instanceof ConfigurationPutRequest.TimezoneOffsetConfigItem) {
                device.addDeviceInfo(new GenericItem(QHybridSupport.ITEM_TIMEZONE_OFFSET, String.valueOf(((ConfigurationPutRequest.TimezoneOffsetConfigItem) item).getValue())));
            }else if(item instanceof ConfigurationPutRequest.BatteryConfigItem){
                device.setBatteryLevel((short) ((ConfigurationPutRequest.BatteryConfigItem) item).getBatteryPercentage());
                device.setBatteryVoltage(((ConfigurationPutRequest.BatteryConfigItem) item).getBatteryVoltage() / 1000f);
                device.setBatteryThresholdPercent((short) 15);

                GBDeviceEventBatteryInfo batteryInfo = new GBDeviceEventBatteryInfo();
                batteryInfo.level = (short) ((ConfigurationPutRequest.BatteryConfigItem) item).getBatteryPercentage();
                batteryInfo.state = BatteryState.BATTERY_NORMAL;
                getAdapter().getDeviceSupport().handleGBDeviceEvent(batteryInfo);
            }else if(item instanceof ConfigurationPutRequest.HeartRateMeasurementModeItem){
                device.addDeviceInfo(new GenericItem(QHybridSupport.ITEM_HEART_RATE_MEASUREMENT_MODE, String.valueOf(((ConfigurationPutRequest.HeartRateMeasurementModeItem) item).getValue())));
            }
        }

        GB.toast("got config", 0, GB.INFO);

        device.sendDeviceUpdateIntent(getAdapter().getContext());
    }
}
