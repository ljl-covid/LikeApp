/*  Copyright (C) 2015-2020 0nse, Andreas Böhler, Andreas Shimokawa,
    Carsten Pfeiffer, Cre3per, Daniel Dakhno, Daniele Gobbetti, Gordon Williams,
    Jean-François Greffier, João Paulo Barraca, José Rebelo, Kranz, ladbsoft,
    Manuel Ruß, maxirnilian, Pavel Elagin, protomors, Quallenauge, Sami Alaoui,
    Sophanimus, tiparega, Vadim Kaushan

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.likeapp.likeapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;

import org.likeapp.likeapp.devices.banglejs.BangleJSCoordinator;
import org.likeapp.likeapp.devices.huami.amazfitbip.AmazfitBipLiteCoordinator;
import org.likeapp.likeapp.devices.huami.amazfitbips.AmazfitBipSCoordinator;
import org.likeapp.likeapp.devices.huami.amazfitgtr.AmazfitGTRCoordinator;
import org.likeapp.likeapp.devices.huami.amazfitgts.AmazfitGTSCoordinator;
import org.likeapp.likeapp.devices.itag.ITagCoordinator;
import org.likeapp.likeapp.devices.jyou.TeclastH30.TeclastH30Coordinator;
import org.likeapp.likeapp.devices.jyou.y5.Y5Coordinator;
import org.likeapp.likeapp.devices.qhybrid.QHybridCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.GBException;
import org.likeapp.likeapp.R;
import org.likeapp.likeapp.database.DBHandler;
import org.likeapp.likeapp.database.DBHelper;
import org.likeapp.likeapp.devices.DeviceCoordinator;
import org.likeapp.likeapp.devices.UnknownDeviceCoordinator;
import org.likeapp.likeapp.devices.casiogb6900.CasioGB6900DeviceCoordinator;
import org.likeapp.likeapp.devices.hplus.EXRIZUK8Coordinator;
import org.likeapp.likeapp.devices.hplus.HPlusCoordinator;
import org.likeapp.likeapp.devices.hplus.MakibesF68Coordinator;
import org.likeapp.likeapp.devices.makibeshr3.MakibesHR3Coordinator;
import org.likeapp.likeapp.devices.hplus.Q8Coordinator;
import org.likeapp.likeapp.devices.huami.amazfitbip.AmazfitBipCoordinator;
import org.likeapp.likeapp.devices.huami.amazfitcor.AmazfitCorCoordinator;
import org.likeapp.likeapp.devices.huami.amazfitcor2.AmazfitCor2Coordinator;
import org.likeapp.likeapp.devices.huami.miband2.MiBand2Coordinator;
import org.likeapp.likeapp.devices.huami.miband2.MiBand2HRXCoordinator;
import org.likeapp.likeapp.devices.huami.miband3.MiBand3Coordinator;
import org.likeapp.likeapp.devices.huami.miband4.MiBand4Coordinator;
import org.likeapp.likeapp.devices.id115.ID115Coordinator;
import org.likeapp.likeapp.devices.jyou.BFH16DeviceCoordinator;
import org.likeapp.likeapp.devices.liveview.LiveviewCoordinator;
import org.likeapp.likeapp.devices.miband.MiBandConst;
import org.likeapp.likeapp.devices.miband.MiBandCoordinator;
import org.likeapp.likeapp.devices.mijia_lywsd02.MijiaLywsd02Coordinator;
import org.likeapp.likeapp.devices.miscale2.MiScale2DeviceCoordinator;
import org.likeapp.likeapp.devices.no1f1.No1F1Coordinator;
import org.likeapp.likeapp.devices.pebble.PebbleCoordinator;
import org.likeapp.likeapp.devices.roidmi.Roidmi1Coordinator;
import org.likeapp.likeapp.devices.roidmi.Roidmi3Coordinator;
import org.likeapp.likeapp.devices.vibratissimo.VibratissimoCoordinator;
import org.likeapp.likeapp.devices.watch9.Watch9DeviceCoordinator;
import org.likeapp.likeapp.devices.xwatch.XWatchCoordinator;
import org.likeapp.likeapp.devices.zetime.ZeTimeCoordinator;
import org.likeapp.likeapp.entities.Device;
import org.likeapp.likeapp.entities.DeviceAttributes;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.impl.GBDeviceCandidate;
import org.likeapp.likeapp.model.DeviceType;

public class DeviceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceHelper.class);

    private static final DeviceHelper instance = new DeviceHelper();
    // lazily created
    private List<DeviceCoordinator> coordinators;

    public static DeviceHelper getInstance() {
        return instance;
    }

    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        for (DeviceCoordinator coordinator : getAllCoordinators()) {
            DeviceType deviceType = coordinator.getSupportedType(candidate);
            if (deviceType.isSupported()) {
                return deviceType;
            }
        }
        return DeviceType.UNKNOWN;
    }

    public boolean getSupportedType(GBDevice device) {
        for (DeviceCoordinator coordinator : getAllCoordinators()) {
            if (coordinator.supports(device)) {
                return true;
            }
        }
        return false;
    }

    public GBDevice findAvailableDevice(String deviceAddress, Context context) {
        Set<GBDevice> availableDevices = getAvailableDevices(context);
        for (GBDevice availableDevice : availableDevices) {
            if (deviceAddress.equals(availableDevice.getAddress())) {
                return availableDevice;
            }
        }
        return null;
    }

    /**
     * Returns the list of all available devices that are supported by Gadgetbridge.
     * Note that no state is known about the returned devices. Even if one of those
     * devices is connected, it will report the default not-connected state.
     *
     * Clients interested in the "live" devices being managed should use the class
     * DeviceManager.
     * @param context
     * @return
     */
    public Set<GBDevice> getAvailableDevices(Context context) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<GBDevice> availableDevices = new LinkedHashSet<GBDevice>();

        if (btAdapter == null) {
            GB.toast(context, context.getString(R.string.bluetooth_is_not_supported_), Toast.LENGTH_SHORT, GB.WARN);
        } else if (!btAdapter.isEnabled()) {
            GB.toast(context, context.getString(R.string.bluetooth_is_disabled_), Toast.LENGTH_SHORT, GB.WARN);
        }
        List<GBDevice> dbDevices = getDatabaseDevices();
        availableDevices.addAll(dbDevices);

        Prefs prefs = GBApplication.getPrefs();
        String miAddr = prefs.getString(MiBandConst.PREF_MIBAND_ADDRESS, "");
        if (miAddr.length() > 0) {
            GBDevice miDevice = new GBDevice(miAddr, "MI", DeviceType.MIBAND);
            availableDevices.add(miDevice);
        }

        String pebbleEmuAddr = prefs.getString("pebble_emu_addr", "");
        String pebbleEmuPort = prefs.getString("pebble_emu_port", "");
        if (pebbleEmuAddr.length() >= 7 && pebbleEmuPort.length() > 0) {
            GBDevice pebbleEmuDevice = new GBDevice(pebbleEmuAddr + ":" + pebbleEmuPort, "Pebble qemu", DeviceType.PEBBLE);
            availableDevices.add(pebbleEmuDevice);
        }
        return availableDevices;
    }

    public GBDevice toSupportedDevice(BluetoothDevice device) {
        GBDeviceCandidate candidate = new GBDeviceCandidate(device, GBDevice.RSSI_UNKNOWN, device.getUuids());
        return toSupportedDevice(candidate);
    }

    public GBDevice toSupportedDevice(GBDeviceCandidate candidate) {
        for (DeviceCoordinator coordinator : getAllCoordinators()) {
            if (coordinator.supports(candidate)) {
                return coordinator.createDevice(candidate);
            }
        }
        return null;
    }

    public DeviceCoordinator getCoordinator(GBDeviceCandidate device) {
        synchronized (this) {
            for (DeviceCoordinator coord : getAllCoordinators()) {
                if (coord.supports(device)) {
                    return coord;
                }
            }
        }
        return new UnknownDeviceCoordinator();
    }

    public DeviceCoordinator getCoordinator(GBDevice device) {
        synchronized (this) {
            for (DeviceCoordinator coord : getAllCoordinators()) {
                if (coord.supports(device)) {
                    return coord;
                }
            }
        }
        return new UnknownDeviceCoordinator();
    }

    public synchronized List<DeviceCoordinator> getAllCoordinators() {
        if (coordinators == null) {
            coordinators = createCoordinators();
        }
        return coordinators;
    }

    private List<DeviceCoordinator> createCoordinators() {
        List<DeviceCoordinator> result = new ArrayList<>();
        result.add(new MiScale2DeviceCoordinator());
        result.add(new AmazfitBipCoordinator());
        result.add(new AmazfitBipLiteCoordinator ());
        result.add(new AmazfitCorCoordinator());
        result.add(new AmazfitCor2Coordinator());
        result.add(new AmazfitGTRCoordinator());
        result.add(new AmazfitGTSCoordinator ());
        result.add(new AmazfitBipSCoordinator ());
        result.add(new MiBand3Coordinator());
        result.add(new MiBand4Coordinator());
        result.add(new MiBand2HRXCoordinator());
        result.add(new MiBand2Coordinator()); // Note: MiBand2 and all of the above  must come before MiBand because detection is hacky, atm
        result.add(new MiBandCoordinator());
        result.add(new PebbleCoordinator());
        result.add(new VibratissimoCoordinator());
        result.add(new LiveviewCoordinator());
        result.add(new HPlusCoordinator());
        result.add(new No1F1Coordinator());
        result.add(new MakibesF68Coordinator());
        result.add(new Q8Coordinator());
        result.add(new EXRIZUK8Coordinator());
        result.add(new TeclastH30Coordinator());
        result.add(new XWatchCoordinator());
        result.add(new QHybridCoordinator());
        result.add(new ZeTimeCoordinator());
        result.add(new ID115Coordinator());
        result.add(new Watch9DeviceCoordinator());
        result.add(new Roidmi1Coordinator());
        result.add(new Roidmi3Coordinator());
        result.add(new Y5Coordinator());
        result.add(new CasioGB6900DeviceCoordinator());
        result.add(new BFH16DeviceCoordinator());
        result.add(new MijiaLywsd02Coordinator());
        result.add(new ITagCoordinator());
        result.add(new MakibesHR3Coordinator());
        result.add(new BangleJSCoordinator());

        return result;
    }

    private List<GBDevice> getDatabaseDevices() {
        List<GBDevice> result = new ArrayList<>();
        try (DBHandler lockHandler = GBApplication.acquireDB()) {
            List<Device> activeDevices = DBHelper.getActiveDevices(lockHandler.getDaoSession());
            for (Device dbDevice : activeDevices) {
                GBDevice gbDevice = toGBDevice(dbDevice);
                if (gbDevice != null && DeviceHelper.getInstance().getSupportedType(gbDevice)) {
                    result.add(gbDevice);
                }
            }
            return result;

        } catch (Exception e) {
            GB.toast("Error retrieving devices from database", Toast.LENGTH_SHORT, GB.ERROR);
            return Collections.emptyList();
        }
    }

    /**
     * Converts a known device from the database to a GBDevice.
     * Note: The device might not be supported anymore, so callers should verify that.
     * @param dbDevice
     * @return
     */
    public GBDevice toGBDevice(Device dbDevice) {
        DeviceType deviceType = DeviceType.fromKey(dbDevice.getType());
        GBDevice gbDevice = new GBDevice(dbDevice.getIdentifier(), dbDevice.getName(), deviceType);
        List<DeviceAttributes> deviceAttributesList = dbDevice.getDeviceAttributesList();
        if (deviceAttributesList.size() > 0) {
            gbDevice.setModel(dbDevice.getModel());
            DeviceAttributes attrs = deviceAttributesList.get(0);
            gbDevice.setFirmwareVersion(attrs.getFirmwareVersion1());
            gbDevice.setFirmwareVersion2(attrs.getFirmwareVersion2());
            gbDevice.setVolatileAddress(attrs.getVolatileIdentifier());
        }

        return gbDevice;
    }

    /**
     * Attempts to removing the bonding with the given device. Returns true
     * if bonding was supposedly successful and false if anything went wrong
     * @param device
     * @return
     */
    public boolean removeBond(GBDevice device) throws GBException {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            BluetoothDevice remoteDevice = defaultAdapter.getRemoteDevice(device.getAddress());
            if (remoteDevice != null) {
                try {
                    Method method = BluetoothDevice.class.getMethod("removeBond", (Class[]) null);
                    Object result = method.invoke(remoteDevice, (Object[]) null);
                    return Boolean.TRUE.equals(result);
                } catch (Exception e) {
                    throw new GBException("Error removing bond to device: " + device, e);
                }
            }
        }
        return false;
    }

}
