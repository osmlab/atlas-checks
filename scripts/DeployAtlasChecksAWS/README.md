# DeployAtlasChecksAWS.py - Atlas Checks on EMR

Programmatically deploy Atlas Checks on AWS cluster

- `DeployAtlasChecksAWS.py` - A python script to be executed on a local server that executes Atlas Checks against input atlas files.
- `configuration.json` - AWS, Spark, Region configuration
- `README.md` - This file to document script usage.

## Prerequisites
Configure [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html).
Python 3.
Install [boto3](https://boto3.amazonaws.com/v1/documentation/api/latest/guide/quickstart.html) Package.

## Configuration
[configuration.json](https://github.com/atlas-generator/blob/dev/scripts/DeployAtlasChecksAWS/configuration.json) is preconfigured for global Atlas Checks.

### Application config
All required Atlas-Checks parameters listed under `configuration` and `border` sections. Optional parameters can be added to the `other` section using the same format: parameter name -> parameter value.

### EMR config
EMR and Spark configurations tuned to generate global Atlas run with 130Xr5d.2xlarge node instances. Changing the default instance type require adjusting spark.config accordingly. Please follow the [best practices](https://aws.amazon.com/blogs/big-data/best-practices-for-successfully-managing-memory-for-apache-spark-applications-on-amazon-emr/).

### Region config
There are 6 predefined regions: America, Europe, Asia, Africa, Oceania or Global. Custom regions can be added with the same format.

## Python3
Make sure python3 is installed. Instructions below for Mac. Also make sure that you source your .zshrc or restart your shell after you perform these steps.

```
brew install pyenv
brew install python
pyenv install 3.8.5
pyenv global 3.8.5
echo -e 'if command -v pyenv 1>/dev/null 2>&1; then\n  eval "$(pyenv init -)"\nfi' >> ~/.zshrc
```

### Python libraries
Install python libraries necessary to execute the application as listed in requirements.txt.
- boto3 - the python libraries to control EC2 instances


```
sudo pip install -r requirements.txt
```

## DeployAtlasChecksAWS.py script parameters
- `--help` - Show help message and exit
mandatory parameters:
- `--input` - S3 path to Atlas Files directory (one up from Country directories).
- `--output` - S3 path Atlas Checks output folder.
mutually exclusive parameters:
- `--country` - Generate single or group of countries by ISO codes
- `--region` - Generate predefined region in [configuration.json](https://github.com/atlas-generator/blob/dev/scripts/DeployAtlasChecksAWS/configuration.json).
required parameters but also configurable in [configuration.json](https://github.com/atlas-generator/blob/dev/scripts/DeployAtlasChecksAWS/configuration.json):
```
"s3":{
         "bucket":"",
         "logging":"",
         "atlas_jar":"",
         "atlas_utilities":""
      }
```
- `--bucket` - S3 bucket name.
- `--log` - S3 path to store EMR logs
- `--jar` - S3 path to atlas-generator jar.
- `--utils` - S3 path to atlas
optional parameters:
- `--config` - Path to [configuration.json](https://github.com/atlas-generator/blob/dev/scripts/DeployAtlasChecksAWS/configuration.json). Default is same directory.
- `--atlasConfig` - Http URL to find configuration file to describe Atlas Checks configuration.
- `--bootstrap` - S3 path to bootstrap script for EMR nodes.

### run DeployAtlasChecksAWS.py script examples
-Single country: USA
`DeployAtlasChecksAWS.py
--country=USA
--zone=your_emr_zone
--bucket=your_bucket_name
--jar=s3://your_bucket_name/Atlas_Checks/jar/atlas-generator-shaded.jar
--util=s3://your_bucket_name/Atlas_Checks/utils
--bootstrap=s3://your_bucket_name/Atlas_Checks/utils/jdk11bootstrap
--atlasConfig=https://raw.githubusercontent.com/osmlab/atlas-checks/dev/config/configuration.json
--input=s3://your_bucket_name/Atlas_Generation/atlas
--output=s3://your_bucket_name/Atlas_Checks/output
--log=s3://your_bucket_name/Atlas_Checks/logging`

-Two countries: USA, CAN.
`DeployAtlasChecksAWS.py
--country=USA,CAN
--zone=your_emr_zone
--bucket=your_bucket_name
--jar=s3://your_bucket_name/Atlas_Checks/jar/atlas-generator-shaded.jar
--util=s3://your_bucket_name/Atlas_Checks/utils
--bootstrap=s3://your_bucket_name/Atlas_Checks/utils/jdk11bootstrap
--atlasConfig=https://raw.githubusercontent.com/osmlab/atlas-checks/dev/config/configuration.json
--input=s3://your_bucket_name/Atlas_Generation/atlas
--output=s3://your_bucket_name/Atlas_Checks/output
--log=s3://your_bucket_name/Atlas_Checks/logging`

-Global Atlas Checks
`DeployAtlasChecksAWS.py
--region=global
--zone=your_emr_zone
--bucket=your_bucket_name
--jar=s3://your_bucket_name/Atlas_Checks/jar/atlas-generator-shaded.jar
--util=s3://your_bucket_name/Atlas_Checks/utils
--bootstrap=s3://your_bucket_name/Atlas_Checks/utils/jdk11bootstrap
--atlasConfig=https://raw.githubusercontent.com/osmlab/atlas-checks/dev/config/configuration.json
--input=s3://your_bucket_name/Atlas_Generation/atlas
--output=s3://your_bucket_name/Atlas_Checks/output
--log=s3://your_bucket_name/Atlas_Checks/logging`
