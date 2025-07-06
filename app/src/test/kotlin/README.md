# Launch Chat tests

## Overview
* Tests are organised under "unittest" and "integrationtest". Do not place any test files directly under the "src/test" package hierarchy.
* The "di" folder at the root of the src/test package defines the Hilt dependency injection configuration for the entire package
* The assumption is that the test files under "unittest" manage the creation of mocks when the class under test has dependencies
* The "di" folder therefore serves the needs of integration tests