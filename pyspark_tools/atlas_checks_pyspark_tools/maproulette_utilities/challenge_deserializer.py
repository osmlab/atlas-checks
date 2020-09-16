from maproulette import ChallengeModel, PriorityRuleModel, PriorityRule, priority_rule
from atlas_checks_pyspark_tools.maproulette_utilities.utilities import *


def get_check_name(data):
    """Returns the check name from a JSON object, if there is one

    :param data: the JSON object
    :return: the check name
    """
    properties = data.get(LogAttributes.PROPERTIES.value)
    if properties:
        return properties.get(LogAttributes.GENERATOR.value)


def get_country_code(data):
    """Returns the ISO3 country code from a JSON object, if there is one

    :param data: the JSON object
    :return: the ISO3 country code
    """
    properties = data.get(LogAttributes.PROPERTIES.value)
    if properties:
        return properties.get(LogAttributes.ISO_COUNTRY_CODE.value)


def difficulty_setter(value):
    """Returns a standardized difficulty setting based on the provided value

    :param value: the difficulty value stored in the checks config.
    :return: A standardized integer value representing the difficulty
    """
    if value == 'EASY':
        return ValidDifficultyValues.EASY.value
    elif value == 'NORMAL':
        return ValidDifficultyValues.NORMAL.value
    elif value == 'EXPERT':
        return ValidDifficultyValues.EXPERT.value


def default_priority_setter(value):
    """Returns a standardized default priority setting based on the provided value

    :param value: the default priority value stored in the checks config.
    :return: A standardized integer value representing the default priority
    """
    if value == 'LOW':
        return ValidDefaultPriorityValues.LOW.value
    elif value == 'MEDIUM':
        return ValidDefaultPriorityValues.MEDIUM.value
    elif value == 'HIGH':
        return ValidDefaultPriorityValues.HIGH.value


def get_priority_rule_string(priority_string):
    """Converts shorthand priority rules into something that MapRoulette expects using PriorityRule and
    PriorityRuleModel objects

    :param priority_string: the priority rule from the checks config
    :return: A PriorityRuleModel containing one or more PriorityRule objects
    """
    if not priority_string:
        return priority_string
    else:
        fixed_rules = list()
        condition = priority_string.get('condition')
        if priority_string.get('rules'):
            for rule in priority_string['rules']:
                if isinstance(rule, dict):
                    fixed_rules.append(rule)
                else:
                    rule_elements = rule.split("=")
                    if len(rule_elements) == 2:
                        rule_key_value = f"{rule_elements[0]}.{rule_elements[1]}"
                        new_priority_rule = PriorityRule(
                            priority_value=rule_key_value,
                            priority_type=priority_rule.Types.STRING,
                            priority_operator=priority_rule.StringOperators.EQUAL
                        )
                        fixed_rules.append(new_priority_rule)
        return PriorityRuleModel(condition=condition, rules=fixed_rules).to_json()


def challenge_deserializer(config_object, check_name):
    """Parses a checks config object in order to obtain various parameters for a challenge

    :param config_object: the checks config object
    :param check_name: the name of the check
    :return: a dict containing challenge parameters
    """
    challenge_items = {
        'name': check_name,
        'description': CheckConfigParameters.DEFAULT_EMPTY_STRING.value,
        'blurb': CheckConfigParameters.DEFAULT_EMPTY_STRING.value,
        'instruction': CheckConfigParameters.DEFAULT_EMPTY_STRING.value,
        'difficulty': ValidDifficultyValues.EASY.value,
        'default_priority': ValidDefaultPriorityValues.LOW.value,
        'high_priority_rule': CheckConfigParameters.DEFAULT_NONE_TYPE.value,
        'medium_priority_rule': CheckConfigParameters.DEFAULT_NONE_TYPE.value,
        'low_priority_rule': CheckConfigParameters.DEFAULT_NONE_TYPE.value,
        'keywords': CheckConfigParameters.DEFAULT_EMPTY_STRING.value,
        'check_in_comment': CheckConfigParameters.DEFAULT_EMPTY_STRING.value
    }

    # Check if there is a matching check in the checks config
    check_params = config_object.get(check_name)
    if check_params:
        # Check whether there is a challenge object in the checks config
        challenge_params = config_object[check_name].get('challenge')
        # If there are challenge params, loop through each one updating the defaults set in challenge_items
        if challenge_params:
            for key, value in challenge_items.items():
                if key == 'difficulty':
                    if challenge_params.get('difficulty'):
                        challenge_items['difficulty'] = difficulty_setter(challenge_params['difficulty'])
                elif key == 'default_priority':
                    if challenge_params.get('defaultPriority'):
                        challenge_items['default_priority'] = default_priority_setter(
                            challenge_params['defaultPriority']
                        )
                elif key == 'high_priority_rule':
                    if challenge_params.get('highPriorityRule'):
                        challenge_items['high_priority_rule'] = get_priority_rule_string(
                            challenge_params['highPriorityRule']
                        )
                elif key == 'medium_priority_rule':
                    if challenge_params.get('mediumPriorityRule'):
                        challenge_items['medium_priority_rule'] = get_priority_rule_string(
                            challenge_params['mediumPriorityRule']
                        )
                elif key == 'low_priority_rule':
                    if challenge_params.get('lowPriorityRule'):
                        challenge_items['low_priority_rule'] = get_priority_rule_string(
                            challenge_params['lowPriorityRule'])
                else:
                    challenge_items[key] = challenge_params.get(key, challenge_items[key])

    return challenge_items


def construct_challenge(config_object, check_name, project_id):
    """Creates a ChallengeModel from a given check in a log file, a project ID, and a checks config file.

    :param config_object: the checks config object
    :param check_name: the name of the check
    :param project_id: the ID of the project to set as the parent ID
    :return: a ChallengeModel object
    """
    challenge_params = challenge_deserializer(config_object, check_name)
    return ChallengeModel(
        parent=project_id,
        name=challenge_params.get('name'),
        description=challenge_params.get('description'),
        blurb=challenge_params.get('blurb'),
        instruction=challenge_params.get('instruction'),
        difficulty=challenge_params.get('difficulty'),
        default_priority=challenge_params.get('default_priority'),
        high_priority_rule=challenge_params.get('high_priority_rule'),
        medium_priority_rule=challenge_params.get('medium_priority_rule'),
        low_priority_rule=challenge_params.get('low_priority_rule'),
        keywords=challenge_params.get('keywords'),
        check_in_comment=challenge_params.get('check_in_comment')
    )
