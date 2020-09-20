# Loadbalancer implementation
### General Remarks
- I have added `junit5` and `assertj` as dependencies. These are the only dependencies in the project.
- you can run `LoadbalancerApp` and it will run through all the steps and print out some things. For simplicity,
I have used system.out (also in the healthcheck).
- inclusion / exclusion is implemented separately. This means that each provider can directly be included again. If a provider
is excluded, it might get automatically included again through the healthcheck, if that responds with OK.
- if a provider is excluded and the healthcheck goes back to ok, it goes from step `EXCLUDED` to `PENDING` and then `OK`.
- There is a `SlowInMemoryProvider` class which simulates a slow Provider. This is needed to show Szenario 8
- Each `ProviderRegistryItem` knows how many requests it is concurrently running at any given time. Overall 
circuit breaker is applied when all providers are in state `BUSY`


