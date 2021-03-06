/*  Copyright (C) 2019-2020 Daniel Dakhno

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
package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil.notification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil.FossilWatchAdapter;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil.file.FilePutRequest;
import org.likeapp.likeapp.util.StringUtils;

public abstract class PlayNotificationRequest extends FilePutRequest {
    public PlayNotificationRequest(int notificationType, int flags, String packageName, FossilWatchAdapter adapter) {
        super((short) 0x0900, createFile(notificationType, flags, packageName, packageName, packageName), adapter);
    }

    public PlayNotificationRequest(int notificationType, int flags, String packageName, String sender, String message, FossilWatchAdapter adapter) {
        super((short) 0x0900, createFile(notificationType, flags, packageName, sender, message), adapter);
    }

    public PlayNotificationRequest(int notificationType, int flags, int packageCRC, String sender, String message, FossilWatchAdapter adapter) {
        super((short) 0x0900, createFile(notificationType, flags, "whatever", sender, message, packageCRC), adapter);
    }

    private static byte[] createFile(int notificationType, int flags, String packageName, String sender, String message){
        CRC32 crc = new CRC32();
        crc.update(packageName.getBytes());
        return createFile(notificationType, flags, packageName, sender, message, (int)crc.getValue());
    }

    private static byte[] createFile(int notificationType, int flags, String title, String sender, String message, int packageCrc) {
        byte lengthBufferLength = (byte) 10;
        byte uidLength = (byte) 4;
        byte appBundleCRCLength = (byte) 4;

        Charset charsetUTF8 = Charset.forName("UTF-8");

        String nullTerminatedTitle = StringUtils.terminateNull(title);
        byte[] titleBytes = nullTerminatedTitle.getBytes(charsetUTF8);
        String nullTerminatedSender = StringUtils.terminateNull(sender);
        byte[] senderBytes = nullTerminatedSender.getBytes(charsetUTF8);
        String nullTerminatedMessage = StringUtils.terminateNull(message);
        byte[] messageBytes = nullTerminatedMessage.getBytes(charsetUTF8);
        if (messageBytes.length > 490) {
            messageBytes = Arrays.copyOf(messageBytes, 490);
        }
        short mainBufferLength = (short) (lengthBufferLength + uidLength + appBundleCRCLength + titleBytes.length + senderBytes.length + messageBytes.length);

        ByteBuffer mainBuffer = ByteBuffer.allocate(mainBufferLength);
        mainBuffer.order(ByteOrder.LITTLE_ENDIAN);

        mainBuffer.putShort(mainBufferLength);

        mainBuffer.put(lengthBufferLength);
        mainBuffer.put((byte) notificationType);
        mainBuffer.put((byte) flags);
        mainBuffer.put(uidLength);
        mainBuffer.put(appBundleCRCLength);
        mainBuffer.put((byte) titleBytes.length);
        mainBuffer.put((byte) senderBytes.length);
        mainBuffer.put((byte) messageBytes.length);

        mainBuffer.putInt(0); // messageId
        mainBuffer.putInt(packageCrc);
        mainBuffer.put(titleBytes);
        mainBuffer.put(senderBytes);
        mainBuffer.put(messageBytes);
        return mainBuffer.array();
    }

}
