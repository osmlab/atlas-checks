# Atlas Checks Best Practices

This document will describe the best practices for writing Atlas Checks.

##### Configuration

It is useful to add various configuration values that will allow you to modify some of the parameters of the check. 
This allows for the ability to run and rerun the check with different parameters producing different results based on the input.
For example the [PoolSizeCheck tutorial](tutorials/tutorial1-PoolSizeCheck.md) allows the user to set the maximum size and minimum
size of the pool to look for. By allowing the user to set these values a user of the check can adjust the settings to 
get just the right minimum and maximum pool size. 

Other reasons for this is to be able to adjust the check for different kinds of scenarios. From a purely analysis
perspective you can use the checks to look for large pools only by setting the minimum value high and the maximum
value a little higher. Alternatively look for small pools only by setting the minimum value low and the maximum value
a little higher. These aren't the most practical examples, however in building checks it is important to think
about these different kind of scenarios.

##### Edge Cases

When building a new check it is important to consider use cases that fall outside of your original intent. By focusing 
on potential edge cases you can ensure that the results of the check will produce less false positives and produce a 
higher quality value of results.

##### Unit Tests

For continued quality control on the checks, building unit tests maintains consistency after updates to various checks.
Updating a check can cause potential unintended side effects, maintaining good unit tests for the check can
ensure that after the update the check is not changed drastically.

##### Code Style

Atlas Checks uses the gradle checkstyle and spotless plugin to make sure that all the code remains consistent throughout. 
The check style plugin is included in the gradle build, so every time you run or build the Check it will 
validate the code style and fail. To easily update your code to adhere to the code style you can run the spotless plugin. 
