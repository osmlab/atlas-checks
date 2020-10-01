# Log Splitter

By default, Atlas Checks line delimited geojson output combines all check outputs into timestamped named .log files. This makes it difficult to parse individual check results. This script converts Atlas Checks line delimited json output files into check separated .log files.

## Dependencies

json, os & argparse - all standard python3 libraries 

## How to Run

```bash
python3 log_splitter.py /path/to/atlas-checks/flag --output /path/to/save/logs/
```
Atlas Checks logs are found under `atlas-checks/build/flag/ISO3`

## Output

Lets say my input is `/Users/abc/github/atlas-checks/build/flag/UNK/` and this folder contains:
 - 1542061154375-135.log
 - 1542090421076-1042.log
 - 1545678119687-135.log

This script will output the following in the output path specified (`/Users/abc/github/atlas-checks/scripts/check-log-splitter/output/`_)
 - DuplicateWaysCheck-56.log               
 - IntersectingBuildingsCheck-51.log       
 - InvalidTurnRestrictionCheck-270.log     
 - SinkIslandCheck-55.log
 - EdgeCrossingEdgeCheck-322.log           
 - InvalidLanesTagCheck-522.log            
 - SelfIntersectingPolylineCheck-36.log
