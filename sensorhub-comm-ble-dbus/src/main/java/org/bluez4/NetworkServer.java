package org.bluez4;
import org.freedesktop.dbus.DBusInterface;
public interface NetworkServer extends DBusInterface
{

  public void Register(String uuid, String bridge);
  public void Unregister(String uuid);

}
