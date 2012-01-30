#
#
# Copyright 2009-2011 Streamsource AB
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Connect to a remote Streamflow instance using JMX-RMI
# Change the host, port and login info to match your environment
package require java
jmx_connect -h localhost -p 1099 -U administrator -P administrator