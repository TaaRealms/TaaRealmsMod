package me.StevenLawson.TotalFreedomMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class TFM_ProtectedArea implements Serializable
{
    private static final long serialVersionUID = -3270338811000937254L;
    public static final double MAX_RADIUS = 50.0D;
    private static Map<String, TFM_ProtectedArea> protectedAreas = new HashMap<String, TFM_ProtectedArea>();
    private final SerializableLocation center;
    private final double radius;

    private TFM_ProtectedArea(Location root_location, double radius)
    {
        this.center = new SerializableLocation(root_location);
        this.radius = radius;
    }

    public static boolean isInProtectedArea(Location location)
    {
        for (Map.Entry<String, TFM_ProtectedArea> protectedArea : TFM_ProtectedArea.protectedAreas.entrySet())
        {
            Location protectedAreaCenter = SerializableLocation.returnLocation(protectedArea.getValue().center);
            if (protectedAreaCenter != null)
            {
                if (location.getWorld() == protectedAreaCenter.getWorld())
                {
                    double protectedAreaRadius = protectedArea.getValue().radius;

                    if (location.distanceSquared(protectedAreaCenter) <= (protectedAreaRadius * protectedAreaRadius))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isInProtectedArea(Vector min, Vector max, String worldName)
    {
        for (Map.Entry<String, TFM_ProtectedArea> protectedArea : TFM_ProtectedArea.protectedAreas.entrySet())
        {
            Location protectedAreaCenter = SerializableLocation.returnLocation(protectedArea.getValue().center);
            if (protectedAreaCenter != null)
            {
                if (worldName.equals(protectedAreaCenter.getWorld().getName()))
                {
                    double sphereRadius = protectedArea.getValue().radius;
                    Vector sphereCenter = protectedAreaCenter.toVector();
                    if (cubeIntersectsSphere(min, max, sphereCenter, sphereRadius))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean cubeIntersectsSphere(Vector min, Vector max, Vector sphere, double radius)
    {
        double d = square(radius);

        if (sphere.getX() < min.getX())
        {
            d -= square(sphere.getX() - min.getX());
        }
        else if (sphere.getX() > max.getX())
        {
            d -= square(sphere.getX() - max.getX());
        }
        if (sphere.getY() < min.getY())
        {
            d -= square(sphere.getY() - min.getY());
        }
        else if (sphere.getY() > max.getY())
        {
            d -= square(sphere.getY() - max.getY());
        }
        if (sphere.getZ() < min.getZ())
        {
            d -= square(sphere.getZ() - min.getZ());
        }
        else if (sphere.getZ() > max.getZ())
        {
            d -= square(sphere.getZ() - max.getZ());
        }

        return d > 0;
    }

    private static double square(double v)
    {
        return v * v;
    }

    public static void addProtectedArea(String label, Location location, double radius)
    {
        TFM_ProtectedArea.protectedAreas.put(label.toLowerCase(), new TFM_ProtectedArea(location, radius));
        saveProtectedAreas();
    }

    public static void removeProtectedArea(String label)
    {
        TFM_ProtectedArea.protectedAreas.remove(label.toLowerCase());
        saveProtectedAreas();
    }

    public static void clearProtectedAreas()
    {
        clearProtectedAreas(false);
    }

    public static void clearProtectedAreas(boolean hard)
    {
        TFM_ProtectedArea.protectedAreas.clear();

        if (!hard)
        {
            autoAddSpawnpoints();
        }

        saveProtectedAreas();
    }

    public static Set<String> getProtectedAreaLabels()
    {
        return TFM_ProtectedArea.protectedAreas.keySet();
    }

    public static void saveProtectedAreas()
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.PROTECTED_AREA_FILE));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(TFM_ProtectedArea.protectedAreas);
            oos.close();
            fos.close();
        }
        catch (Exception ex)
        {
            TFM_Log.severe(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadProtectedAreas()
    {
        try
        {
            File input = new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.PROTECTED_AREA_FILE);
            if (input.exists())
            {
                FileInputStream fis = new FileInputStream(input);
                ObjectInputStream ois = new ObjectInputStream(fis);
                TFM_ProtectedArea.protectedAreas = (HashMap<String, TFM_ProtectedArea>) ois.readObject();
                ois.close();
                fis.close();
            }
        }
        catch (Exception ex)
        {
            File input = new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.PROTECTED_AREA_FILE);
            input.delete();

            TFM_Log.severe(ex);
        }
    }

    public static void autoAddSpawnpoints()
    {
        if (TFM_ConfigEntry.AUTO_PROTECT_SPAWNPOINTS.getBoolean())
        {
            for (World world : Bukkit.getWorlds())
            {
                TFM_ProtectedArea.addProtectedArea("spawn_" + world.getName(), world.getSpawnLocation(), TFM_ConfigEntry.AUTO_PROTECT_RADIUS.getDouble());
            }
        }
    }
}
