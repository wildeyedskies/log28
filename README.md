# log28 - a simple no-frills period tracker for Android

## Database Information

log28 uses [realm](https://realm.io) as it's database. There are 3 "tables"

Category - these are the categories of symptoms. They have a name and boolean for whether they are active.
Symptom - symptoms belong to categories and have names.
DayData - represents the data for a single day. These objects have a list of symptoms that have been recorded that day.
