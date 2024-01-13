# Introduction
(will update and translate)

я за новогодние каникулы запилил очередной pet проект.
суть его в следующем: необходимо шарить скриншоты скидочных карт между членами семьи, а так как магазины стали хитрее, то они просят нас пользоваться приложениями, которые генерят коды.
и так, имеем старый телефон дома, а нем запущено мое приложение, которое эмулируя человека (android accessibility API), запускает приложения магазинов, если нужно нажимает на какие нибудь кнопки там, делает скриншот код скидки и через телеграм посылает нам.
бинго, теперь вся семья может собирать и тратить бонусы. (а на подписке в конкретных магазинах это весьма выгодно получается)

скачать apk можно тут https://lnkd.in/dYYZpMDK (нужно будет настроить приложение как accessibility в телефоне и настроить telegram бота)

дальше допишу инструкции как пользоваться, добавлю еще пару магазинов, вы можете предложить свои
и хочу еще управление звонками и смс добавить, те на телефон приходит смс - а он перекидывает его в телегу
чтобы мы могли указывать эту симку при регистрации во всяких треш сервисах

You can also explore easily more about this repo in this [post](https://link.medium.com/teJIu8DRQBb)

[video1](docs/sharediscountapps2.mp4)
[video2](docs/sharediscountapps1.mp4)
# how install and config app

### install apk to phone
```
adb install example.apk
```
### Setup Accessibility in android
![Step1](docs/step1.jpeg)
![Step2](docs/step2.jpeg)
![Step3](docs/step3.jpeg)
![Step4](docs/step4.jpeg)
![Step5](docs/step5.jpeg)

### Setup Telegram bot

Obtaining a token is as simple as contacting @BotFather, issuing the /newbot command and following the steps until you're given a new token. 
You can find a step-by-step guide https://core.telegram.org/bots/features#creating-a-new-bot.

Your token will look something like this:
```
4839574812:AAFD39kkdpWt3ywyRZergyOLMaJhac60qc
```
set token in app
![set token in app](docs/step6.jpeg)

### allow you and you family to ask codes from app
send any message to your telegram bot and set checkbox true for your name
![permisions](docs/step7.jpeg)
