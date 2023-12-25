# Steam Sale Discord Bot

A java based Discord bot that lets the server know when the games they're keeping track of goes on sale.

### Authors:
* [Jason Ly](https://github.com/jasonly027/)
* [My Nguyen](https://mynguyen.vercel.app/)

## [Invite link](https://discord.com/api/oauth2/authorize?client_id=1186785835866132540&permissions=51200&scope=bot)

## Commands:
* /bind <text_channel> → Set the channel to where sale alerts are sent.
* * By default, sends to the default channel.
* /set_discount_threshold <_percentage_> → Set the minimum discount percentage warranting an alert of an app sale. 
* * By default, the threshold is 1%
* /add_apps <appId,appId,...> → Adds app(s) to list being tracked for sale by server.
* * Add comma separated app IDs to the tracker.
* * App IDs can be retrieved from its respective steam link (in the url)
* /remove_apps <appId,appId,...> → Remove comma separated app IDs from the tracker.
* /search <query> → Search for an app to add to the tracker.
* /list_apps → List all the apps currently being tracked.
* /clear_apps → Clear the tracking list.

## FAQs:
* How often does the bot check for sales?
* * The bot checks for sales every day at about **10:05 AM (PDT)**.
* Why aren't alerts showing up?
* * Reconfigure your discount threshold in case it is too high.
* * Additionally, try rebinding to a text channel.
* The app is still on sale but there wasn't an alert.
* * Alerts for an app are only sent on the first day of a sale duration
* * or, when added *during* a sale, on the following daily check.