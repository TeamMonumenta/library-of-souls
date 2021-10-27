**Library of Souls Update**
The hope here is to make Library of Souls more friendly for an eventual natural spawns replacement.

**SoulGroup:** An interface for zero or more soul entries. Supports getting randomly spawned groups, and average stats on the group.
**SoulPool:** A collection of weighted entries, select one at random.
**SoulParty:** A collection of entries, spawned as one entry.
**SoulEntry:** A single mob. This is how LoS works now.

To work on a pool of mobs:
```
/los addpool <pool label>
/los editpool <pool label> <entry label> <weight, 0 or less removes the entry>
```
Parties have similar commands, just swap the word weight with count.
Entry labels are exactly as they are now for individual mobs, and prefixed with `#` for parties or `~` for pools.
`/los summon <label>` will be modified to accept pools and parties.
Pools and parties will have their edit history tracked, just like individual mobs.

========================================================================================================================

**Parties** spawn as a group, and start with `#`. If an entry of that party is a Pool, Count entries from that pool are selected.
```
los addparty <partyLabel>
los updateparty <partyLabel> <entryLabel> <count>
los delparty <partyLabel>
```
You can check the count of each entry of a party (and click each line to edit) with:
```
los party <partyLabel>
```
**Pools** select a single random entry to spawn, and start with `~`. If the selected entry is a party, the whole party is spawned.
```
los addpool <poolLabel>
los updatepool <poolLabel> <entryLabel> <weight>
los delpool <poolLabel>
```
You can check the weight of each entry of a pool (and click each line to edit) with:
```
los pool <poolLabel>
```
Lastly, you can check the average count of each mob within a group after infinite rolls of random chance with:
```
los averagegroup <groupLabel>
```
