package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class DeterministicAnswerRouter {
    private static final int ACTIVE_RULE_COUNT = 9;

    private DeterministicAnswerRouter() {
    }

    public static int activeRuleCount() {
        return ACTIVE_RULE_COUNT;
    }

    public static boolean isEmergencyRuleId(String ruleId) {
        String normalized = normalize(ruleId);
        return "generic_puncture".equals(normalized);
    }

    public static DeterministicAnswer match(String question) {
        String lower = normalize(question);
        if (isGenericPuncture(lower) && passesSemanticGate("generic_puncture", lower)) {
            return new DeterministicAnswer(
                "generic_puncture",
                "A deep puncture wound is mainly an infection-control problem. Rinse it aggressively, keep it open, and watch closely for worsening infection or a retained foreign body. [GD-023, GD-585, GD-622]\n\n" +
                    "1. Control bleeding first with firm direct pressure. A tourniquet is only for uncontrolled life-threatening limb bleeding. [GD-023]\n" +
                    "2. Flush the wound thoroughly with clean water or cooled boiled water until runoff is clear. Remove only visible loose debris. [GD-585]\n" +
                    "3. Do not probe, widen, or stitch the tract in the field. Dress it so it can drain rather than sealing contamination inside. [GD-622, GD-585]\n" +
                    "4. Change the dressing daily and escalate for worsening pain, swelling, pus, foul odor, fever, red streaking, or a retained object you cannot remove safely. [GD-585, GD-622]",
                buildSources(
                    source("GD-023", "Survival Basics & First 72 Hours", "", "survival", "Control bleeding first, then flush aggressively with clean water."),
                    source("GD-585", "Wound Hygiene, Infection Prevention & Field Sanitation", "", "medical", "Keep puncture tracts draining and watch hard for infection."),
                    source("GD-622", "Animal Bite Wound Care & Rabies Post-Exposure Protocols", "Immediate Wound Care: First 24 Hours", "medical", "Use only as backup for irrigation and daily wound-care discipline.")
                )
            );
        }
        if (isCharcoalSandWaterFilter(lower) && passesSemanticGate("charcoal_sand_water_filter_starter", lower)) {
            return new DeterministicAnswer(
                "charcoal_sand_water_filter_starter",
                "A charcoal-sand filter is a clarification step, not the whole purification plan. Use it to remove sediment first, then disinfect the filtered water before drinking. [GD-423, GD-035]\n\n" +
                    "1. Let muddy water settle first if you can, then decant the clearer water and pass it through a cloth prefilter to keep the main filter from clogging fast. [GD-423, GD-035]\n" +
                    "2. Build the filter around a sand bed supported by gravel and coarse stones, and add activated charcoal as a treatment layer if you have it. Run water through slowly. [GD-423, GD-035]\n" +
                    "3. After filtration, boil, chlorinate, or use another proven disinfection step. The filter improves clarity and taste but does not replace pathogen kill. [GD-035, GD-423]\n" +
                    "4. Store treated water in a clean sealed container and keep clean and dirty handling separate so you do not recontaminate it. [GD-035, GD-423]",
                buildSources(
                    source("GD-423", "Water Chemistry & Treatment", "Filtration Media and Methods", "chemistry", "Use sand and charcoal as clarification layers, then disinfect afterward."),
                    source("GD-423", "Water Chemistry & Treatment", "Simple Field Treatment Sequence", "chemistry", "Treat filtration as one stage in a full safe-water workflow."),
                    source("GD-035", "Water Purification", "Combining Purification Methods", "survival", "Keep clean and dirty handling separate after treatment.")
                )
            );
        }
        if (isReusedContainerWater(lower) && passesSemanticGate("reused_container_water", lower)) {
            return new DeterministicAnswer(
                "reused_container_water",
                "Reused containers are fine for drinking water only when you know they started life as food-safe containers and you can clean and sanitize them thoroughly. Unknown chemical containers are a bad gamble even if they look clean. [GD-619, GD-035]\n\n" +
                    "1. Start with container history. Prefer food-grade plastic, glass, or other containers with known safe prior use. If it held fuel, pesticides, solvents, or anything unknown, do not use it for drinking water. [GD-619, GD-035]\n" +
                    "2. Wash and sanitize aggressively. Scrub with soap and water, rinse repeatedly, then sanitize with a bleach solution or boiling where the material allows. If odor, staining, or residue remains, reject it. [GD-619, GD-035]\n" +
                    "3. Fill only with already-treated water, then seal and label it. Store it cool, dark, and covered so algae, biofilm, and heat damage are less likely. [GD-619, GD-035]\n" +
                    "4. Rotate and inspect the water. If you find slime, sour smell, visible growth, leaks, or persistent off taste, empty it, clean it, and refill with freshly treated water or replace the container. [GD-619, GD-035]",
                buildSources(
                    source("GD-619", "Water Storage & Rationing", "Container Selection, Cleaning, and Rotation", "survival", "Choose only known food-safe containers, then clean, seal, label, and rotate them."),
                    source("GD-035", "Water Purification", "Safe Water Storage After Treatment", "survival", "Dirty or questionable containers can recontaminate treated water."),
                    source("GD-619", "Water Storage & Rationing", "Inspection and Replacement Triggers", "survival", "Reject containers with odor, slime, discoloration, or unknown chemical history.")
                )
            );
        }
        if (isWaterWithoutFuel(lower) && passesSemanticGate("water_without_fuel", lower)) {
            return new DeterministicAnswer(
                "water_without_fuel",
                "Without fuel, treat this as a slow safe-water problem rather than a boiling problem. Settle and pre-filter first, then use sunlight or chemical disinfection if you have it, and do not pretend a plain cloth filter finished the job. [GD-035, GD-423]\n\n" +
                    "1. Reduce turbidity before anything else: let muddy water settle, pour off the clearer layer, and pre-filter through cloth so later treatment works better. Clearer water also improves SODIS and chemical treatment. [GD-035, GD-423]\n" +
                    "2. If you have clear PET or clear glass bottles and real sun, use SODIS. Treat clear water in full sun for about 6-8 hours, or much longer if the water is slightly cloudy or the weather is only partly sunny. Overcast conditions make SODIS a poor bet. [GD-035]\n" +
                    "3. If you have bleach or proper tablets, chemical disinfection may be the faster no-fuel option. Use the right dose and contact time, and remember that dirty or very cold water needs more patience than clean warm water. [GD-035, GD-423]\n" +
                    "4. If chemical contamination or salt is plausible, or if you have no sun and no chemical treatment, finding a better source beats gambling on partial treatment. For longer no-fuel stretches, combine settling, filtration, SODIS, and clean sealed storage rather than relying on one weak step. [GD-035, GD-423]",
                buildSources(
                    source("GD-035", "Water Purification", "Solar Disinfection (SODIS)", "survival", "Clear PET or glass bottles in strong sun give you a real no-fuel disinfection option."),
                    source("GD-035", "Water Purification", "Combining Purification Methods", "survival", "Settle and pre-filter first, then use SODIS or chemical treatment when boiling is unavailable."),
                    source("GD-423", "Water Chemistry & Treatment", "", "chemistry", "Filtration and treatment sequencing matter more when fuel is scarce.")
                )
            );
        }
        if (isFireInRain(lower) && passesSemanticGate("fire_in_rain", lower)) {
            return new DeterministicAnswer(
                "fire_in_rain",
                "Starting a fire in rain is mainly a fuel-prep and shelter problem. Build a dry micro-environment first, then feed a small hot core from the driest material inward. [GD-394, GD-031]\n\n" +
                    "1. Make a dry base and cover before you light anything: use bark, split wood, flat stones, or a log platform to lift the fire off wet ground, and rig a simple rain shield or use natural cover so the first flame is not being washed out. [GD-394, GD-024]\n" +
                    "2. Strip down to the driest fuel you can find: birch bark, resin, inner bark, dry twigs from under cover, feather sticks, and the dry inner core of split branches all matter more than big wet logs. Prepare far more tinder and kindling than you think you need before ignition. [GD-394, GD-024]\n" +
                    "3. Light a small focused core and protect it: once tinder catches, feed pencil-thin dry fuel first, then finger-thick sticks, then larger split wood. Keep the structure tight enough to hold heat but open enough to breathe. [GD-394, GD-031]\n" +
                    "4. After it survives the first minute, turn it into a maintenance fire: add a reflector or cover, keep reserve dry fuel under shelter, and do not smother the fire with big wet wood too early. Fire in rain succeeds by steady heat, not one dramatic flare-up. [GD-024, GD-394]",
                buildSources(
                    source("GD-394", "Swamp & Wetland Survival Systems", "Fire in Wet Conditions", "survival", "Wet fire success starts with dry inner fuel, raised fire beds, and patient fuel staging."),
                    source("GD-394", "Swamp & Wetland Survival Systems", "Fire-Starting in Damp", "survival", "Prepare the bed, feather splits, and shield the first flame from wind and rain."),
                    source("GD-024", "Winter Survival Systems", "Fire Starting in Extreme Conditions", "survival", "Find protected dry tinder and build the fire on a platform instead of wet ground.")
                )
            );
        }
        if (isWeldWithoutWelder(lower) && passesSemanticGate("weld_without_welder_starter", lower)) {
            return new DeterministicAnswer(
                "weld_without_welder_starter",
                "Without an electric welder, start with the simplest joint that fits the load. Mechanical fastening beats a failed hot join, and brazing or soldering are usually safer low-tech options than pretend forge welding. [GD-421, GD-120, GD-281]\n\n" +
                    "1. If bolts, rivets, folded seams, straps, or clamps can handle the job, use them first instead of forcing it into a true weld problem. [GD-120]\n" +
                    "2. If you have heat and filler metal, move up to soldering or brazing. Clean rust, paint, and oil off the joint, fit it tightly, and use flux so oxides do not block bonding. [GD-421, GD-281]\n" +
                    "3. Attempt forge welding only if you have a real forge, clean mild steel, flux, and a solid hammering setup. This is a blacksmithing process, not a campfire shortcut. [GD-421, GD-120]\n" +
                    "4. Test the finished joint before full load and avoid improvised hot joins on pressure vessels, life-safety structures, or unknown alloys. [GD-421]",
                buildSources(
                    source("GD-421", "Welding Metallurgy & Joint Integrity", "Forge Welding (Fire Welding)", "metalworking", "True forge welding needs clean mild steel, flux, and a real forge."),
                    source("GD-421", "Welding Metallurgy & Joint Integrity", "Brazing and Soldering", "metalworking", "Low-tech joining usually means brazing, soldering, or mechanical fastening."),
                    source("GD-120", "Metalworking & Blacksmithing", "", "metalworking", "Use rivets, clamps, straps, and bolted joints before forcing a bad hot join.")
                )
            );
        }
        if (isMetalSplinter(lower) && passesSemanticGate("metal_splinter", lower)) {
            return new DeterministicAnswer(
                "metal_splinter",
                "A small metal splinter is mainly a contamination problem. Clean it, remove only what you can see and grasp, and stop if it is deep or resisting. [GD-736, GD-232]\n\n" +
                    "1. Wash your hands and wash the area thoroughly with soap and clean water. Soak in warm soapy water for a few minutes if that helps the splinter surface. [GD-235, GD-736]\n" +
                    "2. Use clean tweezers to remove only visible metal you can grasp easily. If the tip is just under the surface, a sterilized needle can lift it enough for tweezers. Do not dig blindly or widen the wound. [GD-736, GD-232]\n" +
                    "3. Rinse again, apply antibiotic ointment, and cover the spot with a clean dressing. [GD-736]\n" +
                    "4. Get medical evaluation for retained metal, deep puncture, worsening pain, redness, swelling, pus, or loss of hand function. [GD-736, GD-232]",
                buildSources(
                    source("GD-736", "Splinter and Foreign Body Removal", "", "medical", "Remove only visible splinters you can grasp easily and stop before blind digging."),
                    source("GD-232", "Hand Injuries and Tendon Protection", "", "medical", "Escalate for retained metal, deep puncture, or worsening hand symptoms."),
                    source("GD-235", "Field Wound Cleaning and Dressing", "", "medical", "Wash, rinse, and cover the area after removal.")
                )
            );
        }
        if (isCandlesForLight(lower) && passesSemanticGate("candles_for_light", lower)) {
            return new DeterministicAnswer(
                "candles_for_light",
                "Start with the fuel you actually have. Beeswax makes the easiest clean-burning candle; rendered tallow is the common fallback when you have animal fat but little else. [GD-122, GD-486]\n\n" +
                    "1. Prepare the fuel first: clean beeswax can be softened and reused directly, while tallow needs to be rendered and strained so the candle does not smoke and spit excessively. [GD-122, GD-486]\n" +
                    "2. Make a simple wick from cotton or other twisted plant fiber and keep it centered. A too-thick wick smokes; a too-thin wick drowns in the melt. [GD-122]\n" +
                    "3. Use the simplest build method you can support: roll softened beeswax around the wick, or repeatedly dip the wick in melted tallow or wax until it builds up enough thickness to stand on its own. [GD-122, GD-486]\n" +
                    "4. Treat candles as task lighting, not room lighting: burn them in stable nonflammable holders, trim the wick short, and protect them from drafts. If you only need emergency light and have little wax or fat, a rushlight or simple lamp may be easier than a perfect candle. [GD-294, GD-286]",
                buildSources(
                    source("GD-122", "Lighting Production", "", "crafts", "Beeswax and simple plant-fiber wicks make the easiest starter candles."),
                    source("GD-486", "Animal Fat Rendering and Tallow Uses", "", "agriculture", "Rendered tallow is the fallback candle fuel when wax is scarce."),
                    source("GD-294", "Primitive Lamps, Rushlights, and Wick Lighting", "", "crafts", "Use candles as task lighting and consider rushlights or simple lamps when fuel is limited.")
                )
            );
        }
        if (isGlassmaking(lower) && passesSemanticGate("glassmaking_starter", lower)) {
            return new DeterministicAnswer(
                "glassmaking_starter",
                "The practical beginner target is simple soda-lime glass: silica plus a flux plus lime, melted hot enough to fine and then cooled slowly enough not to shatter. Start there, not with specialty glass. [GD-123, GD-178]\n\n" +
                    "1. Gather the three core ingredients: clean silica sand or crushed quartz for the glass former, soda ash or another sodium-rich flux to lower the melting point, and limestone or shell-derived lime to stabilize the finished glass. [GD-123, GD-178]\n" +
                    "2. Build for heat and containment: you need a refractory-lined furnace and a crucible that can survive sustained very high temperatures. Weak containers or patchy heat waste the whole batch. [GD-123, GD-178]\n" +
                    "3. Melt and refine the batch until it becomes fully molten and the bubbles begin to clear. Do not rush straight from mixed powders to shaping if the melt is still foamy or full of trapped gas. [GD-178, GD-123]\n" +
                    "4. Shape it and anneal it slowly: cast, slump, or gather the melt, then cool it in a controlled gradual way so the piece does not crack from internal stress. Glassmaking fails more often in cooling than in the first melt. [GD-123, GD-178]",
                buildSources(
                    source("GD-123", "Glass, Optics & Ceramics", "", "crafts", "Start with soda-lime glass, a refractory furnace, and deliberate annealing."),
                    source("GD-178", "Alkali & Soda Production", "", "chemistry", "Soda ash and lime matter because raw sand alone does not make practical starter glass."),
                    source("GD-123", "Glass, Optics & Ceramics", "Forming Techniques", "crafts", "Do not skip controlled cooling after shaping.")
                )
            );
        }
        return null;
    }

    private static boolean passesSemanticGate(String ruleId, String lower) {
        switch (normalize(ruleId)) {
            case "generic_puncture":
                return !containsAny(
                    lower,
                    "animal",
                    "bite",
                    "bit",
                    "bit me",
                    "bitten",
                    "dog",
                    "cat",
                    "face",
                    "joint",
                    "mouth",
                    "infected",
                    "infection",
                    "fever",
                    "extract",
                    "remove",
                    "severe bleeding",
                    "uncontrolled bleeding",
                    "hemorrhage",
                    "spurting"
                );
            case "charcoal_sand_water_filter_starter":
                return true;
            case "reused_container_water":
                return !containsAny(
                    lower,
                    "bleach",
                    "fuel",
                    "solvent",
                    "solvents",
                    "pesticide",
                    "pesticides",
                    "paint",
                    "rust",
                    "rusty",
                    "chemical",
                    "chemicals",
                    "barrel",
                    "drum",
                    "metal drum",
                    "steel barrel",
                    "detergent",
                    "cleaner",
                    "cleaning chemical",
                    "cleaning detergent"
                );
            case "water_without_fuel":
                return !containsAny(
                    lower,
                    "sunlight",
                    "well is contaminated",
                    "the well is contaminated",
                    "people are sick",
                    "two people are sick",
                    "wounded",
                    "wounded person"
                );
            case "fire_in_rain":
                return !containsAny(lower, "keep a fire going", "stay warm", "without starting a fire");
            case "weld_without_welder_starter":
                return !containsAny(lower, "screws and bolts", "wood", "wooden", "boards");
            case "metal_splinter":
                return !containsAny(
                    lower,
                    "eye",
                    "eyeball",
                    "vision",
                    "severe bleeding",
                    "uncontrolled bleeding",
                    "hemorrhage",
                    "spurting"
                );
            case "candles_for_light":
                return !containsAny(
                    lower,
                    "buy candles",
                    "burn for",
                    "burn time",
                    "how long do candles burn"
                );
            case "glassmaking_starter":
                return !containsAny(lower, "make a glass", "repair", "cracked", "bottle");
            default:
                return true;
        }
    }

    private static boolean isGenericPuncture(String lower) {
        return containsAny(lower, "puncture wound", "deep puncture", "stepped on a nail", "nail wound", "puncture")
            && !containsAny(lower, "animal bite", "dog bite", "cat bite", "rabies", "bitten by")
            && !containsAny(
                lower,
                "antibiotic", "antibiotics", "infected", "infection", "pus", "red streak",
                "fever", "foreign body", "splinter", "embedded", "remove", "extraction",
                "eye", "eyeball", "chest", "abdomen", "belly", "joint", "mouth", "face",
                "stitch", "stitches", "suture", "sutures", "debridement"
            )
            && !containsAny(lower, "severe bleeding", "uncontrolled bleeding", "won't stop bleeding", "hemorrhage", "spurting");
    }

    private static boolean isCharcoalSandWaterFilter(String lower) {
        return containsAny(
            lower,
            "charcoal sand water filter",
            "charcoal sand filter",
            "build a charcoal sand water filter",
            "make a charcoal sand water filter"
        );
    }

    private static boolean isReusedContainerWater(String lower) {
        boolean waterIntent = containsAny(lower, "water", "drinking water", "stored water");
        boolean storageIntent = containsAny(lower, "store", "storage", "keep", "save", "container", "containers", "bottle", "bottles", "jug", "jugs");
        boolean reusedIntent = containsAny(
            lower,
            "reused", "reuse", "re-use", "used container", "used containers",
            "old bottle", "old bottles", "old soda bottle", "old soda bottles",
            "plastic bottle", "plastic bottles", "soda bottle", "soda bottles",
            "old jug", "old jugs", "milk jug", "milk jugs"
        );
        boolean sanitationIntent = containsAny(lower, "clean", "sanitize", "sanitise", "safe", "food grade", "food-grade");
        boolean excludeDrum = containsAny(lower, "rusty drum", "metal drum", "steel drum", "barrel");
        return waterIntent && (reusedIntent || sanitationIntent) && storageIntent && !excludeDrum;
    }

    private static boolean isWaterWithoutFuel(String lower) {
        boolean waterIntent = containsAny(lower, "water", "drinking water");
        boolean purificationIntent = containsAny(lower, "purify", "purification", "disinfect", "safe to drink", "drink safely", "make safe");
        boolean lowFuelIntent = containsAny(lower, "without fuel", "no fuel", "no fuel for boiling", "can't boil", "cannot boil", "without boiling", "no way to boil");
        boolean excludeScenario = containsAny(
            lower,
            "stay quiet", "keep warm", "getting dark", "wounded", "well is contaminated",
            "the well is contaminated", "people are sick", "two people are sick"
        );
        return waterIntent && purificationIntent && lowFuelIntent && !containsAny(lower, "sunlight") && !excludeScenario;
    }

    private static boolean isFireInRain(String lower) {
        boolean ignitionIntent = containsAny(lower, "start a fire", "build a fire", "make a fire", "light a fire", "ignite");
        boolean wetIntent = containsAny(lower, "rain", "wet", "soaked", "damp", "storm");
        boolean fireIntent = containsAny(lower, "fire", "ignite");
        return fireIntent && wetIntent && ignitionIntent;
    }

    private static boolean isWeldWithoutWelder(String lower) {
        boolean joinIntent = containsAny(lower, "weld", "join", "braze", "solder");
        boolean metalIntent = containsAny(lower, "metal", "steel", "iron", "copper", "brass", "welder");
        boolean noWelder = containsAny(lower, "without a welder", "without welder", "no welder", "without electricity", "weld without a welder");
        return joinIntent && metalIntent && noWelder;
    }

    private static boolean isMetalSplinter(String lower) {
        return containsAny(lower, "metal")
            && containsAny(lower, "splinter")
            && containsAny(lower, "hand", "finger", "palm", "skin")
            && !containsAny(lower, "eye", "eyeball", "vision")
            && !hasMajorBleedingSignal(lower);
    }

    private static boolean isCandlesForLight(String lower) {
        return containsAny(lower, "candle", "candles")
            && containsAny(lower, "make", "making")
            && containsAny(lower, "light");
    }

    private static boolean isGlassmaking(String lower) {
        return containsAny(lower, "glass")
            && containsAny(lower, "make glass", "making glass", "glass from scratch", "glassmaking");
    }

    private static boolean hasMajorBleedingSignal(String lower) {
        return containsAny(
            lower,
            "severe bleeding",
            "uncontrolled bleeding",
            "wont stop bleeding",
            "won't stop bleeding",
            "hemorrhage",
            "spurting"
        );
    }

    private static boolean containsAny(String lower, String... markers) {
        for (String marker : markers) {
            if (matchesMarker(lower, marker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMarker(String normalizedText, String marker) {
        if (normalizedText.isEmpty() || marker == null || marker.isEmpty()) {
            return false;
        }
        String normalizedMarker = normalize(marker);
        if (normalizedMarker.contains(" ")) {
            return normalizedText.contains(normalizedMarker);
        }
        String bounded = " " + normalizedText.replaceAll("[^a-z0-9]+", " ").trim() + " ";
        return bounded.contains(" " + normalizedMarker + " ");
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.US);
    }

    private static SearchResult source(String guideId, String title, String section, String category, String snippet) {
        return new SearchResult(
            title,
            guideId + " | " + category + " | guide-focus",
            snippet,
            "",
            guideId,
            section,
            category,
            "guide-focus"
        );
    }

    private static List<SearchResult> buildSources(SearchResult... results) {
        ArrayList<SearchResult> sources = new ArrayList<>();
        Collections.addAll(sources, results);
        return Collections.unmodifiableList(sources);
    }

    public static final class DeterministicAnswer {
        public final String ruleId;
        public final String answerText;
        public final List<SearchResult> sources;

        DeterministicAnswer(String ruleId, String answerText, List<SearchResult> sources) {
            this.ruleId = ruleId;
            this.answerText = answerText;
            this.sources = sources;
        }
    }
}
