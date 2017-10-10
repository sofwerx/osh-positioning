
package org.sensorhub.impl.sensor.ahrs;

import org.sensorhub.api.comm.ICommProvider;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.sensorML.SMLFactory;
import org.vast.sensorML.SMLHelper;
import net.opengis.sensorml.v20.PhysicalSystem;
import net.opengis.sensorml.v20.SpatialFrame;


public class AHRSSensor extends AbstractSensorModule<AHRSConfig>
{
    static final Logger log = LoggerFactory.getLogger(AHRSSensor.class);
    protected final static String CRS_ID = "SENSOR_FRAME";

    ICommProvider<?> commProvider;
    AHRSOutput dataInterface;


    public AHRSSensor()
    {

    }


    @Override
    public void init() throws SensorHubException
    {
        super.init();
        
        // generate IDs
        generateUniqueID("urn:osh:sensor:ahrs:", null);
        generateXmlID("AHRS_", null);

        dataInterface = new AHRSOutput(this);
        addOutput(dataInterface, false);
        dataInterface.init();
    }


    @Override
    protected void updateSensorDescription()
    {
        synchronized (sensorDescLock)
        {
            super.updateSensorDescription();

            SMLFactory smlFac = new SMLFactory();
            if (!sensorDescription.isSetDescription())
                sensorDescription.setDescription("Microstrain Attitude & Heading Reference System - AHRS");

            // add identifiers
            SMLHelper helper = new SMLHelper(sensorDescription);
            helper.addManufacturerName("Microstrain");
            helper.addModelNumber("3DM-GX2");

            // define mechanical frame
            SpatialFrame localRefFrame = smlFac.newSpatialFrame();
            localRefFrame.setId(CRS_ID);
            localRefFrame.setOrigin("Position of Accelerometers (as marked on the plastic box of the device)");
            localRefFrame.addAxis("X", "The X axis is in the plane of the aluminum mounting plate, parallel to the serial connector (as marked on the plastic box of the device)");
            localRefFrame.addAxis("Y", "The Y axis is in the plane of the aluminum mounting plate, orthogonal to the serial connector (as marked on the plastic box of the device)");
            localRefFrame.addAxis("Z", "The Z axis is orthogonal to the aluminum mounting plate, so that the frame is direct (as marked on the plastic box of the device)");
            ((PhysicalSystem) sensorDescription).addLocalReferenceFrame(localRefFrame);
        }
    }


    @Override
    public void start() throws SensorHubException
    {
        if (commProvider == null)
        {
            // we need to recreate comm provider here because it can be changed by UI
            try
            {
                if (config.commSettings == null)
                    throw new SensorHubException("No communication settings specified");

                commProvider = config.commSettings.getProvider();
                commProvider.start();
            }
            catch (Exception e)
            {
                commProvider = null;
                throw e;
            }
        }

        dataInterface.start(commProvider);

    }


    @Override
    public void stop() throws SensorHubException
    {
        if (dataInterface != null)
            dataInterface.stop();
        
        if (commProvider != null)
        {
            commProvider.stop();
            commProvider = null;
        }
    }


    @Override
    public void cleanup() throws SensorHubException
    {

    }


    @Override
    public boolean isConnected()
    {
        return (commProvider != null);
    }

}
