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
    val bookNames: Map<Int, String> = emptyMap(),
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
        44 to "Romans", 65 to "Revelation",
    )

    private val HindiBooks = mapOf(
        0 to "उत्पत्ति", 1 to "निर्गमन", 2 to "लैव्यव्यवस्था", 3 to "गिनती", 4 to "व्यवस्थाविवरण",
        5 to "यहोशू", 6 to "न्यायियों", 7 to "रूत", 8 to "1 शमूएल", 9 to "2 शमूएल",
        10 to "1 राजा", 11 to "2 राजा", 12 to "1 इतिहास", 13 to "2 इतिहास",
        14 to "एज्रा", 15 to "नहेमायाह", 16 to "एस्तेर", 17 to "अय्यूब", 18 to "भजन संहिता",
        19 to "नीतिवचन", 20 to "सभोपदेशक", 21 to "श्रेष्ठगीत", 22 to "यशायाह",
        23 to "यिर्मयाह", 24 to "विलापगीत", 25 to "यहेजकेल", 26 to "दानिय्येल",
        39 to "मत्ती", 40 to "मरकुस", 41 to "लूका", 42 to "यूहन्ना", 43 to "प्रेरितों के काम",
        44 to "रोमियों", 65 to "प्रकाशितवाक्य",
    )

    private val BanglaBooks = mapOf(
        0 to "আদিপুস্তক", 1 to "যাত্রাপুস্তক", 2 to "লেবীয়পুস্তক", 3 to "গণনাপুস্তক", 4 to "দ্বিতীয় বিবরণ",
        5 to "যিহোশূয়", 6 to "বিচারকচরিত", 7 to "রুথ", 8 to "১ শমূয়েল", 9 to "২ শমূয়েল",
        10 to "১ রাজাবলি", 11 to "২ রাজাবলি", 12 to "১ বংশাবলি", 13 to "২ বংশাবলি",
        14 to "এজরা", 15 to "নেহেমিয়া", 16 to "এস্থার", 17 to "ইয়োব", 18 to "গীতসংহিতা",
        19 to "হিতোপদেশ", 20 to "উপদেশক", 21 to "পরম গীত", 22 to "যিশাইয়",
        23 to "যিরমিয়", 24 to "বিলাপ", 25 to "যিহিষ্কেল", 26 to "দানিয়েল",
        27 to "হোশেয়", 28 to "যোয়েল", 29 to "আমোষ", 30 to "ওবদিয়", 31 to "যোনা",
        32 to "মীখা", 33 to "নাহূম", 34 to "হবক্কূক", 35 to "সফনিয়", 36 to "হগয়",
        37 to "সখরিয়", 38 to "মালাখি", 39 to "মথি", 40 to "মার্ক", 41 to "লূক",
        42 to "যোহন", 43 to "প্রেরিত", 44 to "রোমীয়", 45 to "১ করিন্থীয়", 46 to "২ করিন্থীয়",
        47 to "গালাতীয়", 48 to "ইফিষীয়", 49 to "ফিলিপীয়", 50 to "কলসীয়",
        51 to "১ থিষলনীকীয়", 52 to "২ থিষলনীকীয়", 53 to "১ তীমথিয়", 54 to "২ তীমথিয়",
        55 to "তীত", 56 to "ফিলীমন", 57 to "ইব্রীয়", 58 to "যাকোব", 59 to "১ পিতর",
        60 to "২ পিতর", 61 to "১ যোহন", 62 to "২ যোহন", 63 to "৩ যোহন", 64 to "যিহূদা",
        65 to "প্রকাশিত বাক্য",
    )

    private val KannadaBooks = mapOf(
        0 to "ಆದಿಕಾಂಡ", 1 to "ವಿಮೋಚನಾಕಾಂಡ", 2 to "ಯಾಜಕಕಾಂಡ", 3 to "ಅರಣ್ಯಕಾಂಡ", 4 to "ಧರ್ಮೋಪದೇಶಕಾಂಡ",
        5 to "ಯೆಹೋಶುವ", 6 to "ನ್ಯಾಯಸ್ಥಾಪಕರು", 7 to "ರೂತಳು", 8 to "1 ಸಮುವೇಲನು", 9 to "2 ಸಮುವೇಲನು",
        10 to "1 ಅರಸುಗಳು", 11 to "2 ಅರಸುಗಳು", 12 to "1 ಪೂರ್ವಕಾಲವೃತ್ತಾಂತ", 13 to "2 ಪೂರ್ವಕಾಲವೃತ್ತಾಂತ",
        14 to "ಎಜ್ರನು", 15 to "ನೆಹೆಮಿಯ", 16 to "ಎಸ್ತೇರಳು", 17 to "ಯೋಬನು", 18 to "ಕೀರ್ತನೆಗಳು",
        19 to "ಜ್ಞಾನೋಕ್ತಿಗಳು", 20 to "ಪ್ರಸಂಗಿ", 21 to "ಪರಮ ಗೀತ", 22 to "ಯೆಶಾಯ",
        23 to "ಯೆರೆಮಿಯ", 24 to "ಪ್ರಲಾಪಗಳು", 25 to "ಯೆಹೆಚ್ಚೇಲನು", 26 to "ದಾನಿಯೇಲನು",
        39 to "ಮತ್ತಾಯನು", 40 to "ಮಾರ್ಕನು", 41 to "ಲೂಕನು", 42 to "ಯೋಹಾನನು", 43 to "ಅಪೊಸ್ತಲರ ಕೃತ್ಯಗಳು",
        44 to "ರೋಮಾಪುರದವರಿಗೆ", 65 to "ಪ್ರಕಟನೆ",
    )

    private val MalayalamBooks = mapOf(
        0 to "ഉല്പത്തി", 1 to "പുറപ്പാട്", 2 to "ലേവ്യപുസ്തകം", 3 to "സംഖ്യാപുസ്തകം", 4 to "ആവർത്തനം",
        5 to "യെഹോശുവ", 6 to "ന്യായാധിപന്മാർ", 7 to "രൂത്ത്", 8 to "1 ശമുവേൽ", 9 to "2 ശമുവേൽ",
        10 to "1 രാജാക്കന്മാർ", 11 to "2 രാജാക്കന്മാർ", 12 to "1 ദിനവൃത്താന്തം", 13 to "2 ദിനവൃത്താന്തം",
        14 to "എസ്ര", 15 to "നെഹെമ്യാവ്", 16 to "എസ്തേർ", 17 to "ഇയ്യോബ്", 18 to "സങ്കീർത്തനങ്ങൾ",
        19 to "സദൃശവാക്യങ്ങൾ", 20 to "സഭാപ്രസംഗി", 21 to "ഉത്തമഗീതം", 22 to "യെശയ്യാവ്",
        23 to "യിരെമ്യാവ്", 24 to "വിലാപങ്ങൾ", 25 to "യെഹെസ്കേൽ", 26 to "ദാനീയേൽ",
        39 to "മത്തായി", 40 to "മർക്കോസ്", 41 to "ലൂക്കോസ്", 42 to "യോഹന്നാൻ", 43 to "പ്രവൃത്തികൾ",
        44 to "റോമർ", 65 to "വെളിപാട്",
    )

    private val TamilBooks = mapOf(
        0 to "ஆதியாகமம்", 1 to "யாத்திராகமம்", 2 to "லேவியராகமம்", 3 to "எண்ணாகமம்", 4 to "உபாகமம்",
        5 to "யோசுவா", 6 to "நியாயாதிபதிகள்", 7 to "ரூத்", 8 to "1 சாமுவேல்", 9 to "2 சாமுவேல்",
        10 to "1 இராஜாக்கள்", 11 to "2 இராஜாக்கள்", 12 to "1 நாளாகமம்", 13 to "2 நாளாகமம்",
        14 to "எஸ்றா", 15 to "நெகேமியா", 16 to "எஸ்தர்", 17 to "யோபு", 18 to "சங்கீதம்",
        19 to "நீதிமொழிகள்", 20 to "பிரசங்கி", 21 to "உன்னதப்பாட்டு", 22 to "ஏசாயா",
        23 to "எரேமியா", 24 to "புலம்பல்", 25 to "எசேக்கியேல்", 26 to "தானியேல்",
        39 to "மத்தேயு", 40 to "மாற்கு", 41 to "லூக்கா", 42 to "யோவான்", 43 to "அப்போஸ்தலர் நடபடிகள்",
        44 to "ரோமர்", 65 to "வெளிப்படுத்தின விசேஷம்",
    )

    private val TeluguBooks = mapOf(
        0 to "ఆదికాండము", 1 to "నిర్గమకాండము", 2 to "లేవీయకాండము", 3 to "సంఖ్యాకాండము", 4 to "ద్వితీయోపదేశకాండము",
        5 to "యెహోషువ", 6 to "న్యాయాధిపతులు", 7 to "రూతు", 8 to "1 సమూయేలు", 9 to "2 సమూయేలు",
        10 to "1 రాజులు", 11 to "2 రాజులు", 12 to "1 దినవృత్తాంతములు", 13 to "2 దినవృత్తాంతములు",
        14 to "ఎజ్రా", 15 to "నెహెమ్యా", 16 to "ఎస్తేరు", 17 to "యోబు", 18 to "కీర్తనల గ్రంథము",
        19 to "సామెతలు", 20 to "ప్రసంగి", 21 to "పరమగీతము", 22 to "యెషయా",
        23 to "యిర్మీయా", 24 to "విలాపవాక్యములు", 25 to "యెహెజ్కేలు", 26 to "దానియేలు",
        39 to "మత్తయి", 40 to "మార్కు", 41 to "లూకా", 42 to "యోహాను", 43 to "అపొస్తలుల కార్యములు",
        44 to "రోమీయులకు", 65 to "ప్రకటన గ్రంథము",
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
        bookNames = EnglishBooks,
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
        locale = Locale.forLanguageTag("pl"),
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
        locale = Locale.forLanguageTag("bn-BD"),
        bookNames = BanglaBooks,
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
        locale = Locale.forLanguageTag("hi-IN"),
        bookNames = HindiBooks,
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
        locale = Locale.forLanguageTag("kn-IN"),
        bookNames = KannadaBooks,
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
        locale = Locale.forLanguageTag("ml-IN"),
        bookNames = MalayalamBooks,
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
        locale = Locale.forLanguageTag("ta-IN"),
        bookNames = TamilBooks,
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
        locale = Locale.forLanguageTag("te-IN"),
        bookNames = TeluguBooks,
    )

    fun getStrings(language: String): LocalizedStrings {
        android.util.Log.d("Localization", "Requested language: '$language'")
        return when (language.lowercase().trim()) {
            "polish", "polski", "pl" -> Polish
            "bangla", "bengali", "bn", "ben", "bnangla" -> Bangla
            "hindi", "hi", "hin" -> Hindi
            "kannada", "kn", "kan" -> Kannada
            "malayalam", "ml", "mal" -> Malayalam
            "tamil", "ta", "tam" -> Tamil
            "telugu", "te", "tel" -> Telugu
            else -> English
        }
    }
}
