# Knight Battle Game - Project Documentation

## 📋 Project Overview
- **Platform**: Android (Java)
- **Type**: Turn-based RPG with collection mechanics
- **Current Status**: Fully functional with knight collection, battle system, chest opening, squire mechanics, event system, dual squire support, enhanced animations, and automated documentation

---

## 📄 README PDF Automation

### Automated PDF Generation and Cloud Sync

The project includes **fully automated README-to-PDF conversion** with cloud synchronization for tablet annotation workflows.

#### 🤖 How It Works

1. **README.md updated** → Automatically triggers GitHub Action
2. **PDF generated** using `md-to-pdf` with proper formatting  
3. **PDF uploaded to Dropbox** → Overwrites previous version (no duplicates)
4. **PDF appears on tablet** → Ready for annotation via Dropbox app

#### ⚙️ Technical Implementation

**GitHub Workflow**: `.github/workflows/readme-to-pdf.yml`
- **Trigger**: `on.push.paths: README.md` - Only runs when README changes
- **PDF Engine**: `md-to-pdf` with headless Chrome rendering
- **Cloud Upload**: Dropbox API with overwrite mode for single-file management
- **Runtime**: ~2-3 minutes per conversion
- **Cost**: Free (within GitHub Actions and Dropbox free tiers)

#### 📱 Tablet Workflow

- ✅ **Edit README** → PDF automatically updates in Dropbox
- ✅ **Open Dropbox on tablet** → Latest PDF ready for annotation  
- ✅ **No manual uploads** → Complete automation
- ✅ **No duplicate files** → Always overwrites same file (`Knight-Battle-Game-README.pdf`)

#### 🔧 Setup Requirements

- **Dropbox App** with API token configured as GitHub secret
- **GitHub Actions permissions**: `contents: write`
- **One-time setup** → Ongoing automation

#### 🚧 Implementation Challenges Overcome

- **PDF Generation**: Multiple converter failures resolved with `md-to-pdf`
- **Google Drive API**: Service account storage quota restrictions led to Dropbox adoption
- **YAML Syntax**: JSON formatting in workflow files required careful escaping
- **Action Dependencies**: Non-existent GitHub marketplace actions replaced with direct API calls

This automation eliminates manual PDF generation while providing seamless access to the latest documentation for mobile review and annotation.

---

## 🏗️ System Architecture

### Core Classes
1. **MainActivity** - Main menu and app entry point
2. **GameActivity** - Battle system and combat with enhanced animations
3. **CollectionActivity** - Knight collection management
4. **ChestActivity** - Chest opening and knight acquisition
5. **ProfileActivity** - Player profile and settings
6. **Character** - Battle character with stats and passives
7. **Knight** - Knight data model with database integration
8. **KnightDatabase** - Centralized knight data storage
9. **KnightImageUtils** - Knight image loading system
10. **PassiveManager** - Handles multiple passive effect combinations
11. **EventActivity** - Event display screen
12. **Event** - Event data model
13. **EventDatabase** - Event management and probability

---

## ⚔️ Game Systems

### 1. Fighter/Squire System
**Core Concept**: Players equip knights in different roles:
- **Fighter**: Primary combatant who provides stats (HP, Attack) for battle
- **Squire**: Support knight whose passive abilities enhance the fighter
- **Dual Squire System**: With King's Blessing unlocked, players can equip 2 squires simultaneously

**How It Works**:
- Fighter's buffed stats (including duplicate bonuses) are used for battle
- Squire passive effects are applied to the fighter during combat
- Multiple squires stack their passive effects through PassiveManager
- King's Blessing allows second squire slot to be unlocked

### 2. Knight Collection System
- **12 Knights Total**: 3 Common, 3 Rare, 3 Epic, 3 Legendary
- **Duplicate System**: Up to 11 copies per knight
- **Buffs**: Each duplicate gives +10% HP and Attack (max 100% at 11 copies)
- **Evolution**: Knights with 11 copies can evolve to "Evolved" versions
- **Mass Evolution**: Bulk evolve all eligible knights at once from collection screen (FIXED: No longer loses knights)
- **Rarity Filtering**: Filter collection by Common, Rare, Epic, Legendary, or Evolved knights

### 3. Passive Effects System
**8 Passive Types** with intelligent stacking:
- `HP_BOOST` - Increases max HP (additive stacking)
- `ATTACK_BOOST` - Increases attack damage (additive stacking)
- `DAMAGE_RESISTANCE` - Reduces incoming damage (additive, capped at 95%)
- `EVASION` - Chance to dodge attacks (additive, capped at 90%)
- `CRITICAL_HIT` - Chance to deal 2x damage (additive, capped at 100%)
- `LIFE_STEAL` - Heal for % of damage dealt (additive, capped at 100%)
- `DOUBLE_ATTACK` - Chance to attack twice (additive, capped at 90%)
- `SCALING_ATTACK` - Attack increases as health decreases (uses highest value, no stacking)

**PassiveManager System**:
- Handles multiple passive effects from multiple squires
- Intelligent stacking rules prevent overpowered combinations
- Comprehensive logging for debugging passive interactions
- Recalculates stats when passives are added or removed

### 4. Battle System
- **3 Attack Types**:
  - Light: 50% damage + 50% stun chance
  - Medium: 100% damage
  - Heavy: 200% damage or miss (50/50)
- **Infinite Progression**: 5 stages per world, infinite worlds
- **Scaling Difficulty**: Each world starts with 2x final boss stats from previous world
- **Coin System**: 10 coins per regular boss, 60 coins for mini boss (stage 5)
- **Surrender Option**: Keep coins when surrendering, lose all when dying
- **Enhanced Animations**: Both player and enemy attack animations with proper timing
- **Enemy Stunning**: Light attacks can stun enemies, causing them to skip their turn
- **Attack Spam Prevention**: Buttons disabled during animations and enemy turns to prevent exploitation

### 5. Chest System
- **Cost**: 5 coins per chest, 45 coins for 10x (10% discount)
- **Drop Rates**: 40% Common, 30% Rare, 20% Epic, 10% Legendary
- **Duplicate Handling**: Automatically merges quantities up to 11 max
- **10x Opening**: Bulk chest opening with summary display
- **Rarity Display**: Visual indicators for knight rarities

### 6. Event System
**Random events occur after completing stages to add variety and strategic elements to progression.**

**Event Mechanics**:
- **No events after stage 1**
- **Stage 2+**: Progressive chance - 20% → 40% → 60% for stages 2-4
- **After mini boss (stage 5+)**: 100% chance for Life Tree
- **King's Blessing**: Special unlock event (currently 50% chance for testing)

**Current Events**:
1. **Life Tree**
   - **Effect**: Restores all lost health to full
   - **Rarity**: Common (99% chance when event triggers)
   - **Description**: "A mystical tree restores all your lost health!"

2. **King's Blessing** 
   - **Effect**: Unlocks second squire slot permanently
   - **Rarity**: Legendary (1% base chance, 50% for testing)
   - **Description**: "The ancient king grants you the power to command two squires in battle!"
   - **Persistent**: Once unlocked, stays unlocked forever

**Technical Implementation**:
- Events check in `GameActivity.checkBattleResult()` after stage completion
- Event effects applied immediately before showing event screen
- King's Blessing saves unlock status to SharedPreferences
- Player returns to battle after pressing continue
- Smart event selection prevents duplicate King's Blessing

### 7. Enhanced Animation System
**Complete character animation system for both player and enemy with cinematic battle experience.**

**Player Animation Features**:
- **Entrance Animations**: Slides in from left side of screen
- **Attack Animations**: Knight-specific attack frames during combat
- **Victory Sequence**: Walks off screen to the right after victory
- **Knight-Specific Images**: Uses `knight_name_attack.png` and `knight_name_idle.png`

**Enemy Animation Features** (NEW):
- **Entrance Animations**: Slides in from right side with 200ms delay
- **Attack Animations**: Switches between `enemy_idle.png` and `enemy_attack.png`
- **Defeat Sequence**: Falls down while fading out (dramatic)
- **Proper Timing**: Attack animation plays before damage calculation
- **Optimized Sizing**: Enemy properly sized (300x350dp) and positioned

**Animation Technical Details**:
- **Duration**: 800ms entrance animations with smooth deceleration
- **Attack Timing**: 1000ms attack animations with 150ms damage delay
- **Victory Flow**: Enemy dies → Enemy falls/fades → Player exits right → New enemy enters
- **State Management**: Prevents animation overlaps and spam attacks
- **Resource Management**: Automatic fallback to default images if knight-specific images not found

### 8. Attack Spam Prevention System (NEW)
**Comprehensive system to prevent button spam exploitation during battle.**

**Protection Mechanisms**:
- **Immediate Button Disable**: Attack buttons disabled instantly when attack starts
- **State Validation**: Checks `gameOver`, `isPlayerTurn`, `isPlayerAnimating` before allowing attacks
- **Animation Awareness**: Prevents attacks during player attack animations
- **Turn Management**: Buttons stay disabled during enemy turns
- **Safe Re-enabling**: Buttons only re-enabled when truly player's turn

**Technical Implementation**:
```java
// Spam prevention in attack methods
if (gameOver || !isPlayerTurn || isPlayerAnimating) {
    return; // Block the attack
}
disableAttackButtons(); // Immediate disable
```

**Debug Features**:
- Comprehensive logging of button states
- Attack blocking reasons logged
- Animation state tracking

### 9. Collapsible Passive Display System
**Interactive squire passive information display that can be toggled during battle.**

**UI Features**:
- **Compact State (Default)**: Shows minimal squire count (e.g., "🛡️ 2/2 Squires Active (Tap to expand)")
- **Expanded State**: Shows full squire details with passive names and descriptions
- **Click to Toggle**: Tap the squire text area to switch between compact/expanded
- **Smart Sizing**: Uses `minLines="1"` for compact, `maxLines="8"` for expanded
- **Visual Indicators**: Clear hints about tap functionality and current state

---

## 🗃️ Database Structure

### Knight Database (KnightDatabase.java)
```java
// Example knight entry:
KNIGHT_DATA.put("divine_warrior", new KnightData(
    "divine_warrior",           // ID
    "Divine Warrior",           // Display name
    140,                        // Base HP
    80,                         // Base Attack
    "LEGENDARY",               // Rarity
    new Knight.PassiveEffect(
        "Divine Blessing",      // Passive name
        "Life Steal",          // Type
        "30% life steal",      // Description
        0.30f,                 // Value
        Knight.PassiveType.LIFE_STEAL  // Enum type
    )
));
```

### All Current Knights:
1. **Axolotl Knight** (brave_knight) - Common - HP Boost +15%
2. **Fire Paladin** (fire_paladin) - Common - Attack Boost +10%
3. **Ice Guardian** (ice_guardian) - Common - Damage Resistance 10%
4. **Shadow Warrior** (shadow_warrior) - Rare - Evasion 15%
5. **Earth Defender** (earth_defender) - Rare - Damage Resistance 15%
6. **Berserker Warrior** (berserker_warrior) - Rare - Scaling Attack +5% per 10% health lost
7. **Lightning Striker** (lightning_striker) - Epic - Critical Hit 20%
8. **Vampire Lord** (vampire_lord) - Epic - Life Steal 25%
9. **Wind Tempest** (wind_tempest) - Epic - Double Attack 25%
10. **Phoenix Knight** (phoenix_knight) - Legendary - Critical Hit 35%
11. **Titan Guardian** (titan_guardian) - Legendary - Damage Resistance 30%
12. **Divine Warrior** (divine_warrior) - Legendary - Life Steal 30%

**Enhanced Evolved Versions**:
- All knights can evolve to "Evolved" versions with enhanced passive effects
- Evolved knights get 1.5x to 2x stronger passive abilities
- Evolution requires 11 copies (10 duplicates) of the base knight
- Evolved knights start fresh with quantity 1 and can collect duplicates again

---

## 🎨 Image System

### Image Naming Convention:
- **Player Idle**: `knight_name_idle.png`
- **Player Attack**: `knight_name_attack.png`
- **Enemy Idle**: `enemy_idle.png` (NEW)
- **Enemy Attack**: `enemy_attack.png` (NEW)
- **Format**: Display name → lowercase + underscores

**Examples**:
- "Shadow Warrior" → `shadow_warrior_idle.png`, `shadow_warrior_attack.png`
- "Axolotl Knight" → `axolotl_knight_idle.png`, `axolotl_knight_attack.png`
- Enemy → `enemy_idle.png`, `enemy_attack.png`

**KnightImageUtils Features**:
- Automatic fallback to default images if knight-specific images not found
- Support for evolved knights (uses base knight images)
- Debug logging for image loading
- Resource ID caching for performance

**Location**: `app/src/main/res/drawable/`

---

## 🛠️ Migration System

### General Knight Migration (MainActivity.java)
```java
// Version-less migration system that runs every startup
private void updateKnightStats() {
    // 1. Fix knight name mismatches with database
    fixKnightNameMismatches(editor);
    // 2. Update ALL knights to match database stats
    updateAllKnightsToDatabase(editor);
}
```

### Name Change Mapping:
```java
private String findCorrectKnightName(String oldName) {
    switch (oldName) {
        case "Brave Knight": return "Axolotl Knight";
        // Add more mappings as needed
        default: return null;
    }
}
```

### Duplicate Knight Cleanup:
- Automatic detection and merging of duplicate knight entries
- Quantity consolidation while respecting 11-copy maximum
- Maintains equipment assignments during cleanup

---

## 🔧 How to Make Changes

### Change Knight Stats:
1. Update stats in `KnightDatabase.java`
2. Migration system automatically updates all existing knights

### Change Knight Names:
1. Update name in `KnightDatabase.java` 
2. Add mapping in `findCorrectKnightName()` method
3. Migration system handles the rename for all players

### Change Knight Passives:
1. **Just update passive in `KnightDatabase.java`** - that's it!
2. **No migration needed** - passives load fresh from database
3. **Works instantly** for all existing knights
4. **Evolved knights automatically get enhanced versions**

### Add New Passive Types:
1. **Add to `PassiveType` enum** in `Knight.java`
2. **Implement logic** in `Character.java` methods:
   - `takeDamage()` for defensive passives
   - `performAttack()` for offensive passives  
3. **Add to PassiveManager.java** for stacking rules and caps
4. **Add knights** using new passive to `KnightDatabase.java`
5. **Update evolved effects** in `getEvolvedPassiveEffect()`
6. **Add to chest system** in `ChestActivity.java`

**Complexity varies**: Simple passives (poison, stun) vs Complex passives (shields, turn effects)

### Add New Knight:
1. Add to `KnightDatabase.java` static block
2. Add to `ChestActivity.java` availableKnights list
3. Add images: `knight_name_idle.png`, `knight_name_attack.png`

### Add Knight Images:
1. Name files: `display_name_idle.png`, `display_name_attack.png`
2. Convert spaces to underscores, make lowercase
3. Place in `app/src/main/res/drawable/`

### Add Enemy Animation Images:
1. **Required files**: `enemy_idle.png`, `enemy_attack.png`
2. **Recommended size**: Proportional to 300x350dp display size
3. **Place in**: `app/src/main/res/drawable/`
4. **Animation automatically works** - no code changes needed

### Add New Events:
1. **Add to `EventDatabase.java`** in the static initialization block
2. **Implement effect** in `Event.executeEvent()` method
3. **Handle SharedPreferences** in GameActivity if the event needs to save data
4. **Update event selection logic** in `getEventForStage()` if using special conditions

---

## 🐛 Bug Fixes Applied

### 1. Squire Passive Bug (FIXED)
**Problem**: Squire passives weren't being applied to fighter
**Solution**: Implemented PassiveManager system with proper passive stacking

### 2. Duplicate Knights Bug (FIXED)
**Problem**: Same knight appearing multiple times in collection
**Solution**: Added `fixDuplicateKnights()` method in MainActivity

### 3. Mass Evolution Bug (FIXED)
**Problem**: Mass evolution only evolved first knight, others lost duplicates
**Solution**: Fixed `massEvolveKnights()` to update `owned_knights` string for each knight

### 4. Attack Spam Exploitation (FIXED)
**Problem**: Players could spam attack buttons to kill enemies rapidly
**Solution**: Implemented comprehensive button state management with immediate disabling and safe re-enabling

### 5. Enemy Size and Positioning (FIXED)
**Problem**: Enemy character too large and poorly positioned
**Solution**: Adjusted enemy size to 300x350dp and improved positioning with -100dp margin

### 6. Enemy Animation System (ADDED)
**Problem**: Only player had attack animations
**Solution**: Implemented complete enemy animation system with `enemy_idle.png` and `enemy_attack.png`

---

## 🎮 Admin Features

### Admin Cheat (ProfileActivity.java)
- **Trigger**: Name "admin" + Title "Mighty"
- **Effect**: Temporary "King's Guard" with 1000 HP/Attack and 99% damage resistance
- **Auto-cleanup**: Removed on app restart
- **Special Handling**: Bypasses normal squire passive system

---

## 📱 Current Features

### ✅ Working Systems:
- Complete battle system with 3 attack types and enemy stunning
- Knight collection with 12 unique knights + evolved versions
- Chest opening with proper drop rates and 10x bulk opening
- Fighter/squire equipment system with dual squire support
- Knight evolution at 11 copies with mass evolution option (FIXED)
- Infinite world progression with exponential scaling
- Coin earning and spending with surrender mechanics
- Save/load system with automatic migration
- Knight stat migration and duplicate cleanup
- Custom knight images with fallback system
- Welcome message for new players
- Event system with Life Tree and King's Blessing events
- Enhanced character animations for both player and enemy
- PassiveManager system for complex passive interactions
- Rarity-based collection filtering
- Progress tracking with furthest achievement display
- Collapsible passive display in battle (click to expand/collapse squire info)
- Attack spam prevention system

### 🔄 Recent Updates:
- **FIXED mass evolution bug** - All knights now properly evolve when using bulk evolution
- **ADDED enemy attack animations** - Enemies now animate when attacking using `enemy_idle.png` and `enemy_attack.png`
- **FIXED attack spam exploit** - Players can no longer spam attack buttons to cheat
- **IMPROVED enemy positioning** - Enemy size reduced to 300x350dp and repositioned higher
- **ENHANCED animation system** - Both player and enemy have complete animation cycles
- **ADDED button state management** - Intelligent enabling/disabling during battle phases
- **IMPROVED visual balance** - Better character sizing and positioning for cleaner battle UI

---

## 🚀 Future Considerations

### Potential Improvements:
- **Additional Events**: Treasure Chest, Ancient Shrine, Mysterious Merchant, Cursed Altar
- **Status Effects**: Poison, burn, shield, multi-turn buffs
- **Enhanced UI animations**: More dynamic battle effects, particle systems
- **Sound System**: Battle sounds, chest opening, ambient music
- **Achievement System**: Track milestones and unlock rewards
- **Daily Challenges**: Special battles with unique objectives
- **Settings Menu**: Sound toggle, animation speed, combat preferences
- **Statistics Screen**: Total battles, coins earned, collection progress
- **Guilds/Social**: Share progress, compete with friends
- **More Passive Types**: Reflection damage, temporary immunity, turn-based effects
- **Enemy Variety**: Different enemy types with unique animations and abilities

### Unity Migration Planning:
- Current architecture is Unity-portable
- Clean separation of game logic and UI
- Database system maps well to ScriptableObjects
- Animation system would translate well to Unity's Animator
- PassiveManager system easily adaptable to Unity
- Estimated migration time: 3-4 weekends with guidance

---

## 🔧 Development Tools
- **IDE**: Android Studio
- **Language**: Java
- **Min SDK**: Android API level (check build.gradle)
- **Storage**: SharedPreferences for game data
- **UI**: Native Android Views and Activities
- **Animations**: Android Animation Framework
- **Architecture**: Modular class-based system with database abstraction

---

## 📞 Support Information
- All systems are working and tested
- Code is well-documented with comments
- Migration system handles data updates automatically
- Modular architecture allows easy feature additions
- Event system ready for expansion
- Animation system supports both player and enemy
- PassiveManager system supports complex passive interactions
- Dual squire system fully functional with King's Blessing unlock
- Attack spam prevention ensures fair gameplay

---

## 🌍 Internationalization & RTL Support

### Hebrew Language Support (RTL) - RESOLVED
**Challenge Identified**: Hebrew language users experiencing flipped battle layout
- **Issue**: Android automatically mirrors layouts for Right-to-Left languages
- **Symptom**: Player appears on right side, enemy on left side (reversed from intended)
- **Root Cause**: Hebrew is RTL language, causing LinearLayout with `layout_weight` to flip

**Solution Implemented**: Global RTL Disable
- **Method**: Added `android:supportsRtl="false"` to AndroidManifest.xml
- **Result**: Application no longer supports RTL layout mirroring
- **Impact**: Consistent battle layout across all languages and locales
- **Trade-off**: Hebrew text still displays correctly, but UI layout remains Left-to-Right

**Current Status**: ✅ **FIXED** - Battle positioning now consistent regardless of device language

### RTL Testing Methods:
- Hebrew locale emulator testing
- Developer options "Force RTL layout direction"
- Physical device language switching to Hebrew (עברית)

---

*Last Updated: Added automated README-to-PDF generation with Dropbox sync, fixed Hebrew RTL layout issue, added enemy animation system, fixed mass evolution bug, implemented attack spam prevention, and improved character positioning*
*Status: Fully functional game with comprehensive animation system, robust anti-cheat measures, consistent cross-language layout compatibility, and automated documentation pipeline - ready for advanced feature development and global deployment*
