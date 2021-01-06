# External Data
## Common information
External data is defined as any data that is not an atlas file. External data
_MUST_ be placed in the same directory as the atlas files, with the name
`extra`.

In some cases, if the external data file is _very large_, the data transfer
may be interrupted, and the process will fail, absent error handling.

## Using external data in a check
There must be a constructor for the check that follows this definition:
`public Check(Configuration configuration, ExternalDataFetcher fetcher)`

At this point, the `ExternalDataFetcher` can be used to prefetch data, or
stored later to be used to dynamically get data as the check progresses.
The latter is generally recommended, since the data is cached as it is needed.
This means that the data will not be transferred if the check is cancelled or
has an error prior to needing the data.

### Example code
```java
public Check(Configuration configuration, ExternalDataFetcher fetcher) {
    super(configuration);
    Optional<Resource> optionalResource = fetcher.apply("filename");
    if (optionalResource.isPresent()) {
        // Congratulations, you have the data
        Resource resource = optionalResource.get();
        // At this point, you can get an InputStream.
        InputStream inputStream = resource.read();
        /* Currently, there is no good way to get the actual file on the local
         * filesystem. This should be implemented as soon as that functionality
         * is required.
         *
         * Implementation note: The returned resource will most likely be a
         * InputStreamResource. This, however, can change.
         */
    } else {
        // You don't have the data. Either error out or log, depending upon
        // how critical the data is.
    }
}
```
