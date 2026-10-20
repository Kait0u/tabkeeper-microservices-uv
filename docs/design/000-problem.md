# Tabkeeper - Problem Analysis

Group outings are a great way to spend time, and they can be even better if people participate 
in activities together, such as food, drink or other attractions. Those, however, can get pretty 
taxing mathematically when it's time to settle accounts. It's not so easy to keep track who owes whom, 
and things get even trickier when there were more than one bills, and they were footed by more than one person. 

The solution to that is the app - Tabkeeper. You will be able to record a group's expenses 
and each person will know who they owe and how much, as well as who owes them and how much.  

## Example scenario

Alice, Bob, Celine and Don went on a road trip together.
* Bob paid 60 USD for gas, to be split evenly amongst the participants.
* Alice paid 50 USD for food: hers was 10 USD, Bob's was 15 USD, Celine's was 15 USD and Don's was 10 USD.
* Celine paid 10 USD for her and Don's photo booth session: to be split evenly between them.
* Don paid 8 USD for everyone's ice-cream, to be split evenly between them.

As the result:
* Alice owes 15 USD to Bob, and 2 USD to Don.
* Bob owes 15 USD to Alice, and 2 USD to Don.
* Celine owes 15 USD to Bob, 15 USD to Alice and 2 USD to Don.
* Don owes 15 USD to Bob, 10 USD to Alice, and 5 USD to Celine.

However, we can see that some people owe each other:
* Alice's debt to Bob is 15 USD, and Bob's debt to Alice is also 15 USD, so they can cancel each other out.
* Don owes Bob 15 USD, but Bob owes Don 2 USD for ice-cream: therefore, Don owes 13 USD to Bob.
* Don owes 10 USD to Alice, but Alice owes Don 2 USD for ice-cream: therefore, Don owes 8 USD to Alice.
* Don owes 5 USD to Celine, but Celine owes Don 2 USD for ice-cream: therefore, Don owes 3 USD to Celine.

## Notes

For simplicity, Tabkeeper will work on a FIFO debt settlement basis. That means that for each return 
registered, debts will be settles from oldest to newest.

Each user has the ability to check their balance in every group they belong to. 
Their stats (owing/owed) are aggregated for each group independently. The calculation respects 
the aforementioned FIFO allocation.