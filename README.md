# guessing number game
A small game of clova extension. When the game starts, users can guess a 4-digit number (without repetition) e.g. 3546. 
If the position and value of a digit are the same. It would be A. If the value is the same but the position is different, it would be B. 
For example, if the number is 3546 and user is guessing 3069. The result would be 1A1B.

This game is using [Clova CEK for Kotlin](https://github.com/line/clova-cek-sdk-kotlin) and SpringBoot2 + WebFlux, it is also written in kotlin. 

In this app, it demonstrates how to use ```sessionAttributes``` and ```reprompt``` with the kotlin sdk.

