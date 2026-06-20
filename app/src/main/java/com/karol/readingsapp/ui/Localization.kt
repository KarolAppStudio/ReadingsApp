package com.karol.readingsapp.ui

import java.util.Locale

data class LocalizedStrings(
    val todaysReadings: String,
    val selectedReadings: String,
    val firstReading: String,
    val secondReading: String,
    val thirdReading: String,
    val nextReading: String,
    val noReadings: String,
    val noReadingsShort: String,
    val availableBibles: String,
    val home: String,
    val calendar: String,
    val bible: String,
    val settings: String,
    val about: String,
    val appTitle: String,
    val bibleTranslation: String,
    val saveConfig: String,
    val locale: Locale,
    val bookNames: Map<Int, String> = emptyMap()
)

object Localization {
    // English Book Names (IDs from ReadingRepository)
    private val EnglishBooks = mapOf(
        0 to "Genesis", 1 to "Exodus", 2 to "Leviticus", 3 to "Numbers", 4 to "Deuteronomy",
        5 to "Joshua", 6 to "Judges", 7 to "Ruth", 8 to "1 Samuel", 9 to "2 Samuel",
        10 to "1 Kings", 11 to "2 Kings", 12 to "1 Chronicles", 13 to "2 Chronicles",
        14 to "Ezra", 15 to "Nehemiah", 16 to "Esther", 17 to "Job", 18 to "Psalms",
        19 to "Proverbs", 20 to "Ecclesiastes", 21 to "Song of Solomon", 22 to "Isaiah",
        23 to "Jeremiah", 24 to "Lamentations", 25 to "Ezekiel", 26 to "Daniel",
        39 to "Matthew", 40 to "Mark", 41 to "Luke", 42 to "John", 43 to "Acts",
        44 to "Romans", 65 to "Revelation"
    )

    private val HindiBooks = mapOf(
        0 to "उत्पत्ति", 1 to "निर्गमन", 2 to "लैव्यव्यवस्था", 3 to "गिनती", 4 to "व्यवस्थाविवरण",
        5 to "यहोशू", 6 to "न्यायियों", 7 to "रूत", 18 to "भजन संहिता",
        39 to "मत्ती", 40 to "मरकुस", 41 to "लूका", 42 to "यूहन्ना", 43 to "प्रेरितों के काम"
    )

    private val BanglaBooks = mapOf(
        0 to "আদিপুস্তক", 1 to "যাত্রাপুস্তক", 2 to "লেবীয়পুস্তক", 3 to "গণনাপুস্তক", 4 to "দ্বিতীয় বিবরণ",
        5 to "যিহোশূয়", 6 to "বিচারকচরিত", 7 to "রুথ", 18 to "গীতসংহিতা",
        39 to "মথি", 40 to "মার্ক", 41 to "লূক", 42 to "যোহন", 43 to "প্রেরিত"
    )

    private val KannadaBooks = mapOf(
        0 to "ಆದಿಕಾಂಡ", 1 to "ವಿಮೋಚನಾಕಾಂಡ", 2 to "ಯಾಜಕಕಾಂಡ", 3 to "ಅರಣ್ಯಕಾಂಡ", 4 to "ಧರ್ಮೋಪದೇಶಕಾಂಡ",
        5 to "ಯೆಹೋಶುವ", 6 to "ನ್ಯಾಯಸ್ಥಾಪಕರು", 7 to "ರೂತಳು", 18 to "ಕೀರ್ತನೆಗಳು",
        39 to "ಮತ್ತಾಯ", 40 to "ಮಾರ್ಕ", 41 to "ಲೂಕ", 42 to "ಯೋಹಾನ", 43 to "ಅಪೊಸ್ತಲರ ಕೃತ್ಯಗಳು"
    )

    private val MalayalamBooks = mapOf(
        0 to "ഉല്പത്തി", 1 to "പുറപ്പാട്", 2 to "ലേവ്യപുസ്തകം", 3 to "സംഖ്യാപുസ്തകം", 4 to "ആവർത്തനം",
        5 to "യെഹോശുവ", 6 to "ന്യായാധിപന്മാർ", 7 to "രൂത്ത്", 18 to "സങ്കീർത്തനങ്ങൾ",
        39 to "മത്തായി", 40 to "മർക്കോസ്", 41 to "ലൂക്കോസ്", 42 to "യോഹന്നാൻ", 43 to "അപ്പൊസ്തലപ്രവൃത്തികൾ"
    )

    private val TamilBooks = mapOf(
        0 to "ஆதியாகமம்", 1 to "யாத்திராகமம்", 2 to "லேவியராகமம்", 3 to "எண்ணாகமம்", 4 to "உபாகமம்",
        5 to "யோசுவா", 6 to "நியாயாதிபதிகள்", 7 to "ரூத்", 18 to "சங்கீதம்",
        39 to "மத்தேயு", 40 to "மாற்கு", 41 to "லூக்கா", 42 to "யோவான்", 43 to "அப்போஸ்தலர்"
    )

    private val TeluguBooks = mapOf(
        0 to "ఆదికాండము", 1 to "నిర్గమకాండము", 2 to "లేవీయకాండము", 3 to "సంఖ్యాకాండము", 4 to "ద్వితీయోపదేశకాండము",
        5 to "యెహోషువ", 6 to "న్యాయాధిపతులు", 7 to "రూతు", 18 to "కీర్తనలు",
        39 to "మత్తయి", 40 to "మార్కు", 41 to "లూకా", 42 to "యోహాను", 43 to "అపొస్తలుల కార్యములు"
    )

    private val English = LocalizedStrings(
        todaysReadings = "Today's Readings",
        selectedReadings = "Selected Readings",
        firstReading = "First Reading",
        secondReading = "Second Reading",
        thirdReading = "Third Reading",
        nextReading = "Next Reading",
        noReadings = "No readings scheduled for this section.",
        noReadingsShort = "No readings",
        availableBibles = "Available Bibles",
        home = "Home",
        calendar = "Calendar",
        bible = "Bible",
        settings = "Settings",
        about = "About",
        appTitle = "Daily Reading Companion",
        bibleTranslation = "Bible Translation",
        saveConfig = "Save Configuration",
        locale = Locale.US,
        bookNames = EnglishBooks
    )

    private val Polish = LocalizedStrings(
        todaysReadings = "Dzisiejsze Czytania",
        selectedReadings = "Wybrane Czytania",
        firstReading = "Pierwsze Czytanie",
        secondReading = "Drugie Czytanie",
        thirdReading = "Trzecie Czytanie",
        nextReading = "Następne Czytanie",
        noReadings = "Brak zaplanowanych czytań dla tej sekcji.",
        noReadingsShort = "Brak czytań",
        availableBibles = "Dostępne Biblie",
        home = "Główna",
        calendar = "Kalendarz",
        bible = "Biblia",
        settings = "Ustawienia",
        about = "O aplikacji",
        appTitle = "Codzienny Towarzysz Czytań",
        bibleTranslation = "Tłumaczenie Biblii",
        saveConfig = "Zapisz konfigurację",
        locale = Locale.forLanguageTag("pl-PL")
    )

    private val Bangla = LocalizedStrings(
        todaysReadings = "আজকের পাঠ",
        selectedReadings = "নির্বাচিত পাঠ",
        firstReading = "প্রথম পাঠ",
        secondReading = "দ্বিতীয় পাঠ",
        thirdReading = "তৃতীয় পাঠ",
        nextReading = "পরবর্তী পাঠ",
        noReadings = "এই বিভাগের জন্য কোনো পাঠ নির্ধারিত নেই",
        noReadingsShort = "কোনো পাঠ নেই",
        availableBibles = "উপলব্ধ বাইবেল",
        home = "প্রচ্ছদ",
        calendar = "ক্যালেন্ডার",
        bible = "বাইবেল",
        settings = "সেটিংস",
        about = "সম্পর্কে",
        appTitle = "দৈনিক পাঠ সঙ্গী",
        bibleTranslation = "বাইবেল অনুবাদ",
        saveConfig = "সেভ করুন",
        locale = Locale("bn", "BD"),
        bookNames = BanglaBooks
    )

    private val Hindi = LocalizedStrings(
        todaysReadings = "आज के पाठ",
        selectedReadings = "चयनित पाठ",
        firstReading = "पहला पाठ",
        secondReading = "दूसरा पाठ",
        thirdReading = "तीसरा पाठ",
        nextReading = "अगला पाठ",
        noReadings = "इस अनुभाग के लिए कोई पाठ निर्धारित नहीं है",
        noReadingsShort = "कोई पाठ नहीं",
        availableBibles = "उपलब्ध बाइबिल",
        home = "मुख्य",
        calendar = "कैलेंडर",
        bible = "बाइबिल",
        settings = "सेटिंग्स",
        about = "के बारे में",
        appTitle = "दैनिक पाठ साथी",
        bibleTranslation = "बाइबिल अनुवाद",
        saveConfig = "सहेजें",
        locale = Locale("hi", "IN"),
        bookNames = HindiBooks
    )

    private val Kannada = LocalizedStrings(
        todaysReadings = "ಇಂದಿನ ವಾಚನಗಳು",
        selectedReadings = "ಆಯ್ದ ಓದುಗಳು",
        firstReading = "ಮೊದಲನೆಯ ವಾಚನ",
        secondReading = "ಎರಡನೆಯ ವಾಚನ",
        thirdReading = "ಮೂರನೆಯ ವಾಚನ",
        nextReading = "ಮುಂದಿನ ಓದು",
        noReadings = "ಈ ವಿಭಾಗಕ್ಕೆ ಯಾವುದೇ ಓದುಗಳು ನಿಗದಿಯಾಗಿಲ್ಲ",
        noReadingsShort = "ಓದುಗಳಿಲ್ಲ",
        availableBibles = "ಲಭ್ಯವಿರುವ ಬೈಬಲ್‌ಗಳು",
        home = "ಮುಖಪುಟ",
        calendar = "ಕ್ಯಾಲೆಂಡರ್",
        bible = "ಬೈಬಲ್",
        settings = "ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
        about = "ಬಗ್ಗೆ",
        appTitle = "ದೈನಂದಿನ ಓದುವ ಒಡನಾಡಿ",
        bibleTranslation = "ಬೈಬಲ್ ಅನುವಾದ",
        saveConfig = "ಉಳಿಸಿ",
        locale = Locale("kn", "IN"),
        bookNames = KannadaBooks
    )

    private val Malayalam = LocalizedStrings(
        todaysReadings = "ഇന്നത്തെ വായനകൾ",
        selectedReadings = "തിരഞ്ഞെടുത്ത വായനകൾ",
        firstReading = "ഒന്നാം വായന",
        secondReading = "രണ്ടാം വായന",
        thirdReading = "മൂന്നാം വായന",
        nextReading = "അടുത്ത വായന",
        noReadings = "ഈ വിഭാഗത്തിനായി വായനകളൊന്നും നിശ്ചയിച്ചിട്ടില്ല",
        noReadingsShort = "വായനകളില്ല",
        availableBibles = "ലഭ്യമായ ബൈബിളുകൾ",
        home = "പ്രധാനം",
        calendar = "കലണ്ടർ",
        bible = "ബൈബിൾ",
        settings = "ക്രമീകരണങ്ങൾ",
        about = "കുറിച്ച്",
        appTitle = "ദിനവായന സഹായി",
        bibleTranslation = "ബൈബിൾ വിവർത്തനം",
        saveConfig = "സേവ് ചെയ്യുക",
        locale = Locale("ml", "IN"),
        bookNames = MalayalamBooks
    )

    private val Tamil = LocalizedStrings(
        todaysReadings = "திருப்பலி வாசகங்கள்",
        selectedReadings = "தேர்ந்தெடுக்கப்பட்ட வாசிப்புகள்",
        firstReading = "முதல் வாசகம்",
        secondReading = "இரண்டாம் வாசகம்",
        thirdReading = "மூன்றாம் வாசகம்",
        nextReading = "அடுத்த வாசிப்பு",
        noReadings = "இந்தப் பகுதிக்கு வாசிப்புகள் எதுவும் திட்டமிடப்படவில்லை",
        noReadingsShort = "வாசிப்புகள் இல்லை",
        availableBibles = "கிடைக்கும் பைபிள்கள்",
        home = "முகப்பு",
        calendar = "நாட்காட்டி",
        bible = "வேதாகமம்",
        settings = "அமைப்புகள்",
        about = "பற்றி",
        appTitle = "தினசரி வாసిப்புத் துணை",
        bibleTranslation = "பைபிள் மொழிபெயர்ப்பு",
        saveConfig = "சேமி",
        locale = Locale("ta", "IN"),
        bookNames = TamilBooks
    )

    private val Telugu = LocalizedStrings(
        todaysReadings = "ఈరోజు పఠనాలు",
        selectedReadings = "ఎంచుకున్న పఠనాలు",
        firstReading = "మొదటి పఠనము",
        secondReading = "రెండవ పఠనము",
        thirdReading = "మూడవ పఠనము",
        nextReading = "తదుపరి పఠనం",
        noReadings = "ఈ విభాగానికి ఎటువంటి పఠనాలు షెడ్యూల్ చేయబడలేదు",
        noReadingsShort = "పఠనాలు లేవు",
        availableBibles = "అందుబాటులో ఉన్న బైబిళ్లు",
        home = "మొదటి పేజీ",
        calendar = "క్యాలెండర్",
        bible = "బైబిల్",
        settings = "సెట్టింగ్‌లు",
        about = "గురించి",
        appTitle = "రోజువారీ పఠన తోడు",
        bibleTranslation = "బైబిల్ అనువాదం",
        saveConfig = "సేవ్ చేయండి",
        locale = Locale("te", "IN"),
        bookNames = TeluguBooks
    )

    fun getStrings(language: String): LocalizedStrings {
        return when (language.lowercase()) {
            "polish", "polski", "pl" -> Polish
            "bangla", "bengali", "bn" -> Bangla
            "hindi", "hi" -> Hindi
            "kannada", "kn" -> Kannada
            "malayalam", "ml" -> Malayalam
            "tamil", "ta" -> Tamil
            "telugu", "te" -> Telugu
            else -> English
        }
    }
}
