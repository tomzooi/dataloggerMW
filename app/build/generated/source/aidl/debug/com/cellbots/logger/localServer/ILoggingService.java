/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/tom/AndroidstudioProjects/dataLogger/app/src/main/aidl/com/cellbots/logger/localServer/ILoggingService.aidl
 */
package com.cellbots.logger.localServer;
// Declare the interface.

public interface ILoggingService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cellbots.logger.localServer.ILoggingService
{
private static final java.lang.String DESCRIPTOR = "com.cellbots.logger.localServer.ILoggingService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cellbots.logger.localServer.ILoggingService interface,
 * generating a proxy if needed.
 */
public static com.cellbots.logger.localServer.ILoggingService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cellbots.logger.localServer.ILoggingService))) {
return ((com.cellbots.logger.localServer.ILoggingService)iin);
}
return new com.cellbots.logger.localServer.ILoggingService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_addLogEntry:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.addLogEntry(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cellbots.logger.localServer.ILoggingService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// Insert a log entry from a custom sensor.

@Override public void addLogEntry(java.lang.String sensorName, java.lang.String sensorReadings) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sensorName);
_data.writeString(sensorReadings);
mRemote.transact(Stub.TRANSACTION_addLogEntry, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_addLogEntry = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
// Insert a log entry from a custom sensor.

public void addLogEntry(java.lang.String sensorName, java.lang.String sensorReadings) throws android.os.RemoteException;
}
