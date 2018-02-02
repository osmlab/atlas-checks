# Writing an Atlas Check

### What is an Atlas Check
An Atlas Check is simply an algorithm that you can write that will find and flag issues in the OSM basemap data 
through the use of Atlas, an in memory graph that represents the underlying OSM Basemap data. The framework will 
output GeoJSON files that represent those flagged issues which you can then upload into tools like JOSM, QGis or 
MapRoulette. This will show you where there are issues that require fixing, or simply show you specific information 
about the basemap data. 

### Prerequisites
There are a couple of requirements for building and running a new Atlas Check. The following are required:
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 
    - Click on the link to go and install the latest Java 8 JDK. Follow the instructions for Linux, Mac or Windows operating systems.
- [Gradle](https://gradle.org/install) 
    - Click on the link to go and install the latest version of Gradle. Follow the instructions for Linux, Mac or Windows operating systems
- [Git Command Line Tools](https://git-scm.com/downloads)
    - Click on the link to go and install the latest version of Git. Follow the instructions for Linux, Mac or Windows operating systems.
- Some form of IDE
    - There are multiple editors that you can use to help you build your Atlas Check. You can use text editors like [Notepad(++)](https://notepad-plus-plus.org/), [Vim](http://www.vim.org/download.php), [Emacs](https://www.gnu.org/software/emacs/) or [Sublime](https://www.sublimetext.com). Or you can use a full integrated development environment like [Intellij Idea](https://www.jetbrains.com/idea/) or [Eclipse](https://eclipse.org).

### Building Check Template
Building a check template is as easy as running a very basic command using gradle:
`gradle buildCheck -PCheckName=CheckName`

The value "CheckName" can and should be changed with whatever represents the check that you are planning to 
write more clearly. So if you are writing a check to make sure that any areas tagged as pools are not larger 
than a certain maximum or smaller than a certain minimum, then we might call that check "PoolSizeCheck", 
and would be done like so:
`gradle buildCheck -PCheckName=PoolSizeCheck`

This command would be required to be run inside the Atlas-Checks root folder, and would provide the following updates:
- Creates new file src/main/java/org/openstreetmap/atlas/checks/validation/PoolSizeCheck
- Updates config/configuration.json to include your newly created check.

The new PoolSizeCheck.java file will look like the following:
```java
package org.openstreetmap.atlas.checks.validation;

import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 */
public class PoolSizeCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public PoolSizeCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH, Distance::meters);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // by default we will assume all objects as valid
        return true;
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind flagging the object");
        return Optional.empty();
    }
}
```

Currently it can be run as part of the Atlas Checks framework, but it will essentially not find anything, as you can
see the "flag" function at the bottom always returns Optional.empty(). We will also need to add this to the 
configuration file, but more on that later.

### How to build an Atlas Check

There are three tutorials available giving a step by step break down on how to build an Atlas Check:
- The [PoolSizeCheck](tutorials/tutorial1-PoolSizeCheck.md)
- The [OrphanNodeCheck](tutorials/tutorial2-OrphanNodeCheck.md)
- The [SinkIslandCheck](tutorials/tutorial3-SinkIslandCheck.md)

The Atlas check is split into 3 different sections:
1. Initialize 
2. Validate 
3. Generate
 
#### Initialize
During the "Initialize" phase our check will be instantiated by the framework using a default constructor 
`public PoolSizeCheck(final Configuration configuration)`, and this is where we would set any variables 
for use across all the objects. In our PoolSizeCheck example we would set our maximum and minimum sizes 
obtained from configuration. This stage is only called once per check right in the beginning when the 
framework is initializing all checks.

#### Validate
During the "Validate" phase our check will validate whether the object in question should even be checked, 
using the function `public boolean validCheckForObject(final Atlas object)`. Generally speaking this 
would be a fairly quick check so as not to waste our time checking every object or feature.  

#### Generate
During the "Generate" phase our check will execute the algorithm that will look for objects or features 
that need to be flagged. In our example case this would be features that are tagged as pools that are 
smaller than a certain size or larger than a certain size.

#### Configuration

Configuration is defined in the following file [configuration.json](../config/configuration.json). This file contains
all the configuration values for the system and the checks themselves. For more detailed information see 
[configuration.md](configuration.md)

#### Results

To execute our check(s), we simply need to run the following gradle command:
`gradle run`

This will execute our flag using the default configuration. For executing with advanced properties see 
[Atlas Checks Standalone Application](standalone.md).

The result flags will output file with a single GeoJSON output on each line equivalent to a single 
flag found by the checks. Each flag on each line will look like the following below:
```json
{
    "features": [
        {
            "geometry": {
                "coordinates": [
                    [
                        -63.0252213,
                        18.2071873
                    ],
                    [
                        -63.0252611,
                        18.2072548
                    ],
                    [
                        -63.0253496,
                        18.2072094
                    ],
                    [
                        -63.0252962,
                        18.2071416
                    ],
                    [
                        -63.0252735,
                        18.2071573
                    ],
                    [
                        -63.0252699,
                        18.2071422
                    ],
                    [
                        -63.0252119,
                        18.2071691
                    ],
                    [
                        -63.0252213,
                        18.2071873
                    ]
                ],
                "type": "LineString"
            },
            "properties": {
                "ItemId": "362203839000000",
                "ItemType": "Area",
                "access": "private",
                "addr:city": "Long Path",
                "iso_country_code": "AIA",
                "last_edit_changeset": "32865224",
                "last_edit_time": "1437805947000",
                "last_edit_user_id": "1996480",
                "last_edit_user_name": "MichNicole",
                "last_edit_version": "1",
                "leisure": "swimming_pool",
                "osmid": "362203839",
                "source": "Bing"
            },
            "type": "Feature"
        }
    ],
    "properties": {
        "generator": "PoolSizeCheck",
        "id": "362203839000000",
        "instructions": "1. The swimming pool with OSM ID 362203839 with a surface area of 107.36 meters squared is greater than the expected maximum of 100.0 meters squared.",
        "timestamp": "Fri Jun 16 14:08:40 PDT 2017"
    },
    "type": "FeatureCollection"
}
```

The properties of each flag will contain the following items:
- generator - The class name of the check that created the flag
- id - A unique identifier for the flag
- instructions - The instructions for each flag that is defined by the check
- timestamp - The time that the check was found.

### Currently Available Checks

- [PoolSizeCheck](tutorials/tutorial1-PoolSizeCheck.md)
- [BuildingRoadIntersectionCheck](checks/buildingRoadIntersectionCheck.md)
- [SelfIntersectingPolylineCheck](checks/selfIntersectingPolylineCheck.md)
- [FloatingEdgeCheck](checks/floatingEdgeCheck.md)
- [RoundAboutClosedLoopCheck](checks/roundaboutClosedLoopCheck.md)
- [SharpAngleCheck](checks/sharpAngleCheck.md)
- [SinkIslandCheck](tutorials/tutorial3-SinkIslandCheck.md)
- [SnakeRoadCheck](checks/snakeRoadCheck.md)
- [DuplicateNodeCheck](checks/duplicateNodeCheck.md)
- [OrphanNodeCheck](tutorials/tutorial2-OrphanNodeCheck.md)
- [InvalidTurnRestrictionCheck](checks/invalidTurnRestrictionCheck.md)

** For Best Practices around writing Atlas Checks, please view our [best practices document](bestpractices.md). **

### Debugging and Unit Tests

For information on debugging Atlas Checks please see [Debugging Altas Checks](debugging.md)
For information around writing unit tests for Atlas Checks see [Writing Unit Tests](unit_tests.md) 
