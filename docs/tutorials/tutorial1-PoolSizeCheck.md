# Tutorial 1
### How to build a PoolSizeCheck Atlas Check

For reference around general development see [The Development Document](../dev.md), for this tutorial it is assumed
that you have read through that document.

For reference on complete source code see [PoolSizeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/PoolSizeCheck.java)
 
#### Initializing our PoolSizeCheck
The first thing we need to do for our PoolSizeCheck is to initialize it. This will involve building out the
constructor for the class that is required. If we do not require any configuration properties from the configuration
file, then it would simply be a matter of creating the default constructor. If we did require some configuration
properties, we would build the constructor and initialize our private variables for later use in the Check.

In our PoolSizeCheck we want to initialize two separate variables:
1. maximumSize - The maximum size in meters squared that we would expect a pool to be.
2. minimumSize - The minimum size in meters squared that we would expect a pool to be.

```java
// The worlds largest swimming pool is at the San Alfonso del Mar resort in Algarrobo and
// measures 1,013 meters in length, which is 4,856,227.71 square meters. So we can use a even
// 5,000,000 and assume that it won't find any valid pools. In saying that we can modify
// configuration later in countries outside Chile to get more of a standard norm, and if need be
// we can also filter the pool out of our equation.
public static final double MAXIMUM_SIZE_DEFAULT = 5000000;
// A 5 meter squared pool if a circle would only be roughly 2 meters in diameter.
public static final double MINIMUM_SIZE_DEFAULT = 5;
// Create maximum and minimum size variables to be used later in our flag function
private final double maximumSize;
private final double minimumSize;
public PoolSizeCheck(final Configuration configuration)
{
    super(configuration);
    // Retrieve the maximum and minimum sizes from configuration
    this.maximumSize = (double) this.configurationValue(configuration, "surface.maximum", MAXIMUM_SIZE_DEFAULT);
    this.minimumSize = (double) this.configurationValue(configuration, "surface.minimum", MINIMUM_SIZE_DEFAULT);
}
```

1. Add default constant variables MAXIMUM_SIZE_DEFAULT and MINIMUM_SIZE_DEFAULT to define the default values for
our size variables. This is so that if the configuration is not set we can still run the check and just assume
a safe value to use. 
2. Add final variables for maximumSize and minimumSize
3. Create PoolSizeCheck constructor
4. Retrieve maximumSize and minimumSize from configuration defaulting to maximum and minimum defaults if not 
found in configuration. We are using the key "surface.maximum" and "surface.minimum", these keys can be anything
that would make sense to you as long it is reflected in the configuration file.

#### Validate the incoming 
```java
//add above in import section
import org.openstreetmap.atlas.geography.atlas.items.Area;

public boolean validCheckForObject(final AtlasObject object)
{
    return object instanceof Area;
}
```

For our PoolSizeCheck we simply need to check if the object from the Atlas that is being returned is of type Area.
All swimming pools would be expected to be of type Area. Atlas has 6 types that you can check for:
- Edge - generally speaking navigable ways
- Area - closed polygons, eg. buildings, administrative boundaries, parking lots etc.
- Line - non-navigable ways. eg. powerlines, fences, etc
- Point - A non-navigable Node
- Node - A navigable node
- Relation - Same as relations in OSM.

Technically we could check for the size of the area in this function as well, however that could decrease the
performance of the check and the goal of this function is to quickly evaluate whether the object could be a
potential problem object. So removing all other features but areas allows us to execute the more performance 
intensive code over a smaller problem set. 

#### Generate the Check
```java
//add above in import section
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

protected Optional<CheckFlag> flag(final AtlasObject object)
{
    final Area area = (Area) object;
    if (Validators.isOfType(object, LeisureTag.class, LeisureTag.SWIMMING_POOL))
    {
        final double surfaceArea = area.asPolygon().surface().asMeterSquared();
        if (surfaceArea > this.maximumSize)
        {
            return Optional.of(this.createFlag(object,
                    String.format(
                            "The OSM area with tag 'leisure=swimming_pool' and ID %s with a surface area of %.2f meters squared is greater than the expected maximum of %s meters squared.",
                            object.getOsmIdentifier(), surfaceArea, this.maximumSize)));
        }
        else if (surfaceArea < this.minimumSize)
        {
            return Optional.of(this.createFlag(object,
                    String.format(
                            "The OSM area with tag 'leisure=swimming_pool' and ID %s with a surface area of %.2f meters squared is smaller than the expected minimum of %s meters squared.",
                            object.getOsmIdentifier(), surfaceArea, this.minimumSize)));
        }
    }
    return Optional.empty();
}
```

Let's step through the code:
1. We first cast the object to an Area object, we know that it is an area object, as in our ValidCheckForObject function
we checked whether it was an object already. The ValidCheckForObject function always is executed prior to the flag function.
2. We then create an if statement and check whether the leisure=swimming_pool tag is on the object. We could 
do this in the ValidCheckForObject function, either way works just as well. Another object
would be to use the "tags.filter" configuration option that will filter all the objects by a tag filter. For the PoolSizeCheck
 we could use "tags.filter":"leisure->swimming_pool". By doing this we could forgo our if statement entirely. However
 we actually don't want to make this configurable, as our check is specific to pools and we wouldn't really want
 that to change, so in this particular case that would not be a good option.
3. We then get the polygon of the area and calculate the surface area in meters squared using the handy 
`surface().asMeterSquared()` function
4. Next we create an if else statement one checking whether the surface area is larger than expected, the
other checking whether it is smaller than expected. We could very easily use && and place this in a single 
if statement. In that case we would make our instruction less generic, either way works and is up to the 
developer of the check.

#### Setup Configuration

Now that we have created our check we need to update the configuration so that we can define our maximum
and minimum sizes as needed. By default the Atlas Checks framework will use reflection to pick up the
check, and so without modifying any configuration your check would automatically run along with all the 
other checks. We however are going to change that behavior and run only our check.

First we need to modify this [configuration.json](../../config/configuration.json) file. We want to change the following things:
1. On line 7 we want to change the value from true to false. This will disable all checks by default and require
any checks to explicitly be turned on, which we will do in the next step.
2. Below line 8 we are going to add the following code:
```json
{
  "PoolSizeCheck": {
    "enabled": true,
    "surface": {
      "maximum": 1000.0,
      "minimum": 50.0
    }
  }
}
```

The properties for the new check make sure it is enabled, and sets our values for the maximum and minimum surface areas
for the pool.

#### Run your correction

Now that you have built your correction, the next step is simply to run it. And this is all explained [here](../standalone.md).
