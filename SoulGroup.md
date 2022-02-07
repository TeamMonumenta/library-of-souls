# Library of Souls Update
The hope here is to make Library of Souls more friendly for an eventual natural spawns replacement.

## Glossary

**SoulGroup:** An collection for zero or more soul groups. Supports getting random groups of entries, and average entries stats in the group. They can be summoned in a bounding box with `/los summongroup <groupLabel> <pos1> <pos2>`. Every group has its history tracked to make rolling back changes easier.
**SoulEntry:** A single mob, and the simplest group. These have no prefix.
**SoulPool:** A collection of weighted groups, select one at random. These have a label prefix of `~`.
**SoulParty:** A collection of groups, all spawned together. These have a label prefix of `#`.

## Soul Parties

To start, create an empty party:
```
/los addparty <partyLabel>
```

Then, you can add soul groups to the party. All groups within the party will be spawned `<count>` times. To remove a group, set its count to 0.
```
/los updateparty <partyLabel> <groupLabel> <count>
```

You can list the groups currently in a party with:
```
/los party <partyLabel>
```

If you decide to remove a party, and are sure it is not used anywhere, you can delete it. If it is still used somewhere, it will be treated as an empty group.
```
/los delparty <partyLabel>
```

## Soul Pools

To start, create an empty pool:
```
/los addpool <poolLabel>
```

Then, you can add soul groups to the pool. The chance of any group spawning is `<weight>` out of the total of each `<weight>` in the pool. For example, if `alice` has a weight of 1 and `bob` has a weight of 3, Bob will spawn 75% of the time, and Alice will spawn the other 25%. Having a group of more than one SoulEntry does not affect the weight of that group. To remove a group, set its weight to 0.
```
/los updatepool <poolLabel> <groupLabel> <weight>
```

You can list the groups currently in a pool with:
```
/los pool <poolLabel>
```

If you decide to remove a pool, and are sure it is not used anywhere, you can delete it. If it is still used somewhere, it will be treated as an empty group.
```
/los delpool <poolLabel>
```

## Statistics

You can check the average count of each SoulEntry within a group as if there were infinite rolls of random chance with:
```
los averagegroup <groupLabel>
```
