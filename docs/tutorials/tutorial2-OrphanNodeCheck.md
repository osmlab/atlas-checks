# Tutorial 2
### How to build a OrphanNodeCheck Atlas Check

For reference around general development see [The Development Document](../dev.md), for this tutorial it is assumed
that you have read through that document.

For reference on complete source code see [OrphanNodeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/OrphanNodeCheck.java)

###### *NOTE

In this document the terms Node and Point are used seemingly interchangeably. This is because in OSM there is only
a Node type. In the Atlas there is a Node and Point type. A Node object in the Atlas is part of a navigable way and a Point
is part of a non-navigable way. In OSM there is no differentiation.
 
#### Initializing our OrphanNodeCheck
The first thing we need to do for our OrphanNodeCheck is to initialize it. This will involve building out the
constructor for the class that is required. If we do not require any configuration properties from the configuration
file, then it would simply be a matter of creating the default constructor. If we did require some configuration
properties, we would build the constructor and initialize our private variables for later use in the Check. For our
OrphanNodeCheck we don't need to get any specific variables from configuration.

```java
public OrphanNodeCheck(final Configuration configuration)
{
    super(configuration);
}
```

#### Validate the incoming Object
```java
//add above in import section
import org.openstreetmap.atlas.geography.atlas.items.Area;

public boolean validCheckForObject(final AtlasObject object)
{
    return object instanceof Point && object.getOSMTags().size() == 0
                           && ((Point) object).relations().size() == 0;
}
```

For the OrphanNodeCheck, we can find Orphaned nodes at the same time we check for the validity of the object. 
We will define our orphan node as a Atlas Point with no tags or relations associated with it. We can do this 
because an Atlas Point is defined as a non-navigable way.

#### Generate the Check
```java
protected Optional<CheckFlag> flag(final AtlasObject object)
{
    return Optional.of(this.createFlag(object,
                    String.format(
                            "Node with OSM ID %s is an orphan, no tags and not connected to any ways.",
                            object.getOsmIdentifier())));
}
```

We have determined whether or not the Atlas Object is an orphan node already within the ValidCheckForObject 
function, and so in our flag function where we generate the flag we don't need any logic to figure out 
more details about the object or walk the network to check other parameters to make sure it is flag, we
simply create the flag with the useful `createFlag` function defined in the BaseCheck.

#### Setup Configuration

Even though this check is very simple and has no configuration values that we require, we will add it 
to the configuration for demonstration purposes.

First we need to modify this [configuration.json](../../config/configuration.json) file. We want to change the following things:
1. On line 7 we want to change the value from true to false. This will disable all checks by default and require
any checks to explicitly be turned on, which we will do in the next step.
2. Below line 8 we are going to add the following code:
```json
{
  "OrphanNodeCheck": {
    "enabled": true
  }
}
```

The properties for the new check explicitly set it as enabled.

#### Run your correction

Now that you have built your correction, the next step is simply to run it. And this is all explained [here](../standalone.md).
