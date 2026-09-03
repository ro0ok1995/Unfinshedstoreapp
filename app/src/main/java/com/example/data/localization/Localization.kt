package com.example.data.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppLanguage(val code: String, val titleAr: String, val titleEn: String, val direction: LayoutDirection) {
    ARABIC("ar", "العربية", "Arabic", LayoutDirection.Rtl),
    ENGLISH("en", "English", "English", LayoutDirection.Ltr);

    companion object {
        fun fromCode(code: String): AppLanguage =
            if (code.lowercase() == "en") ENGLISH else ARABIC
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ARABIC }
val LocalStrings = compositionLocalOf { ArabicStrings as Strings }

interface Strings {
    // General
    val appName: String
    val customer: String
    val currencySymbol: String
    val cancel: String
    val confirm: String
    val save: String
    val delete: String
    val edit: String
    val archive: String
    val restore: String
    val search: String
    val all: String
    val active: String
    val archived: String
    val deleted: String
    val loading: String
    val emptyState: String
    val errorOccurred: String
    val success: String
    val optional: String
    val required: String
    val notes: String
    val date: String
    val status: String
    val actions: String
    val details: String
    val share: String
    val print: String
    val exportPdf: String
    val generateReport: String

    // Bottom Navigation & Drawer
    val navHome: String
    val navAccounts: String
    val navPurchases: String
    val navStatements: String
    val navDatabase: String
    val navSettings: String
    val navAnalysisCenter: String
    val navMore: String

    // Global Drawer
    val drawerSectionMain: String
    val drawerSectionAnalysis: String
    val drawerSectionManagement: String
    val drawerHome: String
    val drawerAccounts: String
    val drawerPurchases: String
    val drawerAnalysis: String
    val drawerMore: String
    val drawerStats: String
    val drawerStatements: String
    val drawerReports: String
    val drawerShopInfo: String
    val drawerAppSettings: String
    val drawerDataCenter: String
    val drawerAboutApp: String

    // Quick Action Center (+)
    val quickActionTitle: String
    val quickActionNewPurchase: String
    val quickActionNewPurchaseDesc: String
    val quickActionQuickPayment: String
    val quickActionQuickPaymentDesc: String
    val selectCustomerForPayment: String

    // Home Screen (Phase 2)
    val homeShopMode: String
    val homeShopModeDesc: String
    val homeCustomerMode: String
    val clearCustomerSelection: String
    val periodToday: String
    val periodThisWeek: String
    val periodThisMonth: String
    val periodCustom: String
    val periodAllTime: String
    val selectDateRange: String
    val startDateLabel: String
    val endDateLabel: String
    val tabAnalysisCenter: String
    val tabAccountStatement: String
    val sectionOverview: String
    val sectionFinancialStats: String
    val sectionCustomerDebt: String
    val sectionCashPayments: String
    val sectionTransactions: String
    val sectionPeriodAnalysis: String
    val totalSalesLabel: String
    val cashSalesLabel: String
    val creditSalesLabel: String
    val paymentsCollectedLabel: String
    val totalCustomersLabel: String
    val activeCustomersLabel: String
    val collectionRateLabel: String
    val averageDebtLabel: String
    val breakdownTitle: String
    val salesVsPaymentsTitle: String
    val debtOverviewTitle: String
    val noChartData: String
    val runningBalanceLabel: String
    val orderNewestFirst: String
    val orderOldestFirst: String
    val viewingWholeShop: String
    val viewingSpecificCustomer: String
    val switchToWholeShop: String
    val switchCustomer: String
    val exportAnalysisSummary: String
    val sortOrderLabel: String
    val homeStatsCredit: String
    val homeStatsCash: String
    val homeStatsPayments: String
    val homeStatsBalance: String
    val homeStatsTotal: String
    val latestActivities: String
    val viewFullHistory: String
    val noActivitiesInPeriod: String
    val noStatsInPeriod: String
    val recordFirstTransaction: String
    val notifications: String
    val noNotifications: String
    val markAllAsRead: String
    val clearAllNotifications: String
    val setAsHomeCustomer: String
    val viewCustomerProfile: String
    val customerContextActive: String

    // Home Screen
    val homeTitle: String
    val totalDebtSummary: String
    val totalCustomersWithDebt: String
    val searchCustomerHint: String
    val noDebtsFound: String
    val recordPaymentShort: String
    val newPurchaseShort: String
    val quickStatsTodayCredit: String
    val quickStatsTodayPayments: String
    val quickStatsTodayCash: String
    val customersList: String
    val allCustomersSettled: String
    val debtAmount: String
    val outstandingDebt: String
    val noCustomersYet: String
    val noCustomersYetDesc: String
    val noMatchingCustomers: String
    val settledDebt: String

    // Purchases Screen
    val purchasesTitle: String
    val selectCustomer: String
    val purchaseType: String
    val creditPurchase: String
    val cashPurchase: String
    val selectCustomerRequiredForCredit: String
    val chooseProducts: String
    val customItem: String
    val itemName: String
    val unitPrice: String
    val quantity: String
    val subtotal: String
    val addItem: String
    val orderItems: String
    val emptyCart: String
    val totalAmount: String
    val recordPurchaseButton: String
    val purchaseSuccessMessage: String
    val noCustomerSelected: String
    val cashCustomerWalkIn: String
    val recordTransactionFor: String
    val clearCart: String
    val completeTransaction: String
    val cartEmptyWarning: String
    val reviewTransactionTitle: String
    val confirmTransaction: String
    val creditPurchaseNotice: String
    val cashPurchaseNotice: String
    val searchProductsPlaceholder: String
    val selectCustomerPrompt: String
    val changeCustomer: String
    val chooseCustomerDropdown: String

    // Settlement Modes
    val settlementMode: String
    val settlementFullDebt: String
    val settlementFullDebtDesc: String
    val settlementFullCash: String
    val settlementFullCashDesc: String
    val settlementPartial: String
    val settlementPartialDesc: String
    val paidCashAmount: String
    val remainingDebtAmount: String
    val partialPaymentAmountPrompt: String
    val partialPaymentInvalid: String

    // Quick Payment
    val quickPaymentTitle: String
    val quickPayment: String
    val quickPaymentSelectCustomer: String
    val noDebtForCustomer: String
    val balanceAfterPayment: String
    val viewCustomerStatement: String
    val quickPaymentSuccessTitle: String
    val paymentSuccessTitle: String
    val previousDebtLabel: String
    val newDebtLabel: String

    // Payments
    val recordPaymentTitle: String
    val recordPayment: String
    val paymentAmount: String
    val maxPaymentIsDebt: String
    val paymentExceedsDebtError: String
    val paymentSuccessMessage: String
    val payFullDebt: String

    // Statements & Transactions
    val statementsTitle: String
    val filterType: String
    val filterStatus: String
    val completedTransactions: String
    val cancelledTransactions: String
    val cancelTransaction: String
    val cancelTransactionPrompt: String
    val cancellationReason: String
    val transactionCancelledSuccess: String
    val restoreTransaction: String
    val restoreTransactionPrompt: String
    val transactionRestoredSuccess: String
    val exportStatementsPdf: String
    val noTransactionsFound: String
    val noTransactionsOfType: String
    val noCustomersAvailable: String
    val noCustomersFoundSearch: String
    val searchCustomerStatementPlaceholder: String
    val transactionTypeCredit: String
    val transactionTypeCash: String
    val transactionTypePayment: String
    val moreDetails: String
    val transactionDetailsTitle: String
    val allCustomersMode: String
    val selectCustomerForStatement: String
    val clearCustomerFilter: String
    val customerLabel: String
    val paymentMethod: String
    val timeLabel: String
    val statusLabel: String

    // Customer Details
    val customerDetailsTitle: String
    val primaryActionsTitle: String
    val recordPurchase: String
    val recordPurchaseDesc: String
    val viewAccountStatement: String
    val viewAccountStatementDesc: String
    val recordPaymentDesc: String
    val accountFullyPaid: String
    val confirmAndSave: String
    val amountMustBeGreaterThanZero: String
    val customerPhone: String
    val customerAddress: String
    val customerNotFound: String
    val customerNotes: String
    val currentDebt: String
    val callCustomer: String
    val exportCustomerStatementPdf: String
    val customerTransactionHistory: String
    val customerAccountStatement: String
    val transactionHistory: String
    val allTransactions: String
    val totalPurchases: String
    val totalPaid: String
    val totalPayments: String
    val payment: String
    val cancelled: String
    val cashCustomer: String
    val editCustomer: String
    val archiveCustomer: String
    val deleteCustomer: String
    val customerArchivedBadge: String
    val cannotTransactArchivedCustomer: String

    // Database / Management
    val databaseTitle: String
    val databaseSubtitle: String
    val tabCustomers: String
    val tabProducts: String
    val tabBackup: String
    val tabReports: String
    val statusActive: String
    val statusArchived: String
    val statusDeleted: String
    val addCustomer: String
    val editCustomerDialogTitle: String
    val addProduct: String
    val editProductDialogTitle: String
    val customerName: String
    val phoneNumber: String
    val productName: String
    val defaultPrice: String
    val productUnit: String
    val productCategory: String
    val noCustomersFound: String
    val noProductsFound: String
    val searchProductHint: String
    val selectProduct: String
    val editQuantity: String
    val enterQuantity: String
    val selectProductImage: String
    val changeProductImage: String
    val removeProductImage: String
    val productImage: String
    val restoreCustomerConfirm: String
    val restoreProductConfirm: String
    val permanentDeleteWarning: String

    // Backup & Restore
    val backupTitle: String
    val createBackup: String
    val restoreBackup: String
    val selectBackupFile: String
    val createBackupButton: String
    val restoreBackupButton: String
    val backupDescription: String
    val restoreDescription: String
    val backupCreatedSuccess: String
    val backupRestoredSuccess: String
    val invalidBackupFile: String
    val restoreWarning: String
    val shareBackupFile: String

    // PDF Reports
    val reportsTitle: String
    val generatePdfReports: String
    val reportCustomerStatement: String
    val reportCustomerStatementDesc: String
    val reportAllCustomersDebts: String
    val reportAllDebts: String
    val reportAllDebtsDesc: String
    val reportProductSales: String
    val reportProductSalesDesc: String
    val reportAllProductsCatalog: String
    val reportProductCatalog: String
    val reportProductCatalogDesc: String
    val generatePdf: String
    val generatingPdf: String
    val pdfGeneratedSuccess: String
    val selectProductForReport: String

    // Settings
    val settingsTitle: String
    val settingsSubtitle: String
    val languageSectionTitle: String
    val languageSection: String
    val arabicLanguage: String
    val englishLanguage: String
    val currentLanguage: String
    val shopInfoSectionTitle: String
    val shopInfoSection: String
    val shopName: String
    val shopPhone: String
    val shopAddress: String
    val saveShopInfo: String
    val shopInfoSaved: String
    val currencySectionTitle: String
    val currencySection: String
    val currencyDescription: String
    val fixedCurrencyNote: String
    val dangerZoneSection: String
    val factoryReset: String
    val factoryResetPrompt: String
    val resetAllData: String
    val resetAllDataConfirm: String
    val aboutSection: String
    val aboutDesc: String

    // Settings 6 Tabs & Details
    val settingsTabAccount: String
    val settingsTabData: String
    val settingsTabArchive: String
    val settingsTabAppearance: String
    val settingsTabReports: String
    val settingsTabAbout: String
    val ownerManagerName: String
    val storeAddress: String
    val storePhone: String
    val storeNameRequiredNotice: String
    val backupAndRestoreTitle: String
    val createBackupDesc: String
    val restoreBackupDesc: String
    val backupInspectTitle: String
    val backupInspectDate: String
    val backupInspectCustomers: String
    val backupInspectProducts: String
    val backupInspectTransactions: String
    val confirmRestoreAction: String
    val restoreSafetyNotice: String
    val archiveSectionCustomers: String
    val archiveSectionProducts: String
    val archiveSectionTransactions: String
    val cancelledTxNotice: String
    val permanentDeleteConfirmPrompt: String
    val themeTitle: String
    val themeLight: String
    val themeDark: String
    val themeSystem: String
    val themeLightDesc: String
    val themeDarkDesc: String
    val themeSystemDesc: String
    val displayModeTitle: String
    val themesTitle: String
    val themesSubtitle: String
    val themePurple: String
    val themeGold: String
    val themeBlackAndWhite: String
    val themePurpleDesc: String
    val themeGoldDesc: String
    val themeBlackAndWhiteDesc: String
    val appVersionLabel: String
    val appVersionValue: String
    val developerLabel: String
    val developerValue: String
    val privacyPolicyTitle: String
    val privacyPolicyContent: String
    val termsTitle: String
    val termsContent: String
    val contactDevTitle: String
    val contactDevContent: String

    // Extra database & settings strings
    val editProduct: String
    val archiveProduct: String
    val deleteProduct: String
    val appNameFull: String
    val close: String
    val noResultsMessage: String
    val storeDetailsInfoNotice: String
    val storeInfoTitle: String
    val storeName: String
    val ownerName: String
    val saveStoreDetails: String
    val dangerZoneTitle: String
    val resetAllDataDesc: String
    val confirmRestoreTitle: String
    val restoreInspectNotice: String
    val backupDateLabel: String
    val confirmAndRestore: String
    val resetAllDataWarning: String
    val noArchiveRecords: String
    val noArchiveCustomersDesc: String
    val noArchiveProductsDesc: String
    val noCancelledTransactionsNotice: String
    val pdfReportsCenterNotice: String
    val termsOfUseTitle: String
    val termsOfUseContent: String
    val contactDeveloperTitle: String
    val contactDeveloperContent: String
    val developerInfo: String
    val languageTitle: String
    val unknownCustomer: String
}

object ArabicStrings : Strings {
    override val appName = "حسابات المحل"
    override val customer = "العميل"
    override val currencySymbol = "₪"
    override val cancel = "إلغاء"
    override val confirm = "تأكيد"
    override val save = "حفظ"
    override val delete = "حذف"
    override val edit = "تعديل"
    override val archive = "أرشفة"
    override val restore = "استعادة"
    override val search = "بحث..."
    override val all = "الكل"
    override val active = "النشطة"
    override val archived = "المؤرشفة"
    override val deleted = "المحذوفة"
    override val loading = "جاري التحميل..."
    override val emptyState = "لا توجد بيانات متاحة حالياً"
    override val errorOccurred = "حدث خطأ، يرجى المحاولة مرة أخرى"
    override val success = "تمت العملية بنجاح"
    override val optional = "اختياري"
    override val required = "مطلوب"
    override val notes = "ملاحظات"
    override val date = "التاريخ"
    override val status = "الحالة"
    override val actions = "إجراءات"
    override val details = "التفاصيل"
    override val share = "مشاركة"
    override val print = "طباعة"
    override val exportPdf = "تصدير PDF"
    override val generateReport = "إنشاء التقرير PDF"

    override val navHome = "الرئيسية"
    override val navAccounts = "الحسابات"
    override val navPurchases = "المشتريات"
    override val navStatements = "كشف الحساب"
    override val navDatabase = "قاعدة البيانات"
    override val navSettings = "الإعدادات"
    override val navAnalysisCenter = "مركز التحليل"
    override val navMore = "المزيد"

    override val drawerSectionMain = "الرئيسية"
    override val drawerSectionAnalysis = "التحليل"
    override val drawerSectionManagement = "الإدارة"
    override val drawerHome = "الرئيسية"
    override val drawerAccounts = "الحسابات"
    override val drawerPurchases = "المشتريات"
    override val drawerAnalysis = "مركز التحليل"
    override val drawerMore = "المزيد"
    override val drawerStats = "الإحصائيات"
    override val drawerStatements = "كشف الحساب"
    override val drawerReports = "التقارير"
    override val drawerShopInfo = "معلومات المحل"
    override val drawerAppSettings = "إعدادات التطبيق"
    override val drawerDataCenter = "مركز البيانات"
    override val drawerAboutApp = "حول التطبيق"

    override val quickActionTitle = "إجراء سريع"
    override val quickActionNewPurchase = "تسجيل مشتريات / معاملة"
    override val quickActionNewPurchaseDesc = "فاتورة جديدة، بيع آجل أو نقدي مع الأصناف"
    override val quickActionQuickPayment = "تسجيل دفعة سريعة"
    override val quickActionQuickPaymentDesc = "سداد نقدي مباشر لزبون مدين"
    override val selectCustomerForPayment = "اختر الزبون لتسجيل الدفعة"

    override val homeShopMode = "الوضع العام للمحل"
    override val homeShopModeDesc = "إحصائيات شاملة ونشاطات كافة الزبائن"
    override val homeCustomerMode = "سياق حساب العميل"
    override val clearCustomerSelection = "إلغاء التحديد"
    override val periodToday = "اليوم"
    override val periodThisWeek = "هذا الأسبوع"
    override val periodThisMonth = "هذا الشهر"
    override val periodCustom = "فترة مخصصة"
    override val periodAllTime = "كافة الفترات"
    override val selectDateRange = "تحديد النطاق الزمني"
    override val startDateLabel = "تاريخ البدء"
    override val endDateLabel = "تاريخ الانتهاء"
    override val tabAnalysisCenter = "مركز التحليل والإحصائيات"
    override val tabAccountStatement = "كشف الحساب والسجلات"
    override val sectionOverview = "نظرة عامة"
    override val sectionFinancialStats = "الإحصائيات المالية"
    override val sectionCustomerDebt = "ديون الزبائن"
    override val sectionCashPayments = "السيولة النقدية والتحصيل"
    override val sectionTransactions = "حركة المعاملات"
    override val sectionPeriodAnalysis = "تحليل الفترة الزمنية"
    override val totalSalesLabel = "إجمالي المبيعات"
    override val cashSalesLabel = "المبيعات النقدية"
    override val creditSalesLabel = "المبيعات الآجلة"
    override val paymentsCollectedLabel = "الدفعات المحصلة"
    override val totalCustomersLabel = "إجمالي الزبائن"
    override val activeCustomersLabel = "الزبائن النشطون"
    override val collectionRateLabel = "نسبة التحصيل"
    override val averageDebtLabel = "متوسط الدين للزبون"
    override val breakdownTitle = "توزيع العمليات والسيولة"
    override val salesVsPaymentsTitle = "مقارنة المبيعات والدفعات"
    override val debtOverviewTitle = "نظرة عامة على ديون المحل"
    override val noChartData = "لا توجد عمليات مسجلة خلال هذه الفترة"
    override val runningBalanceLabel = "الرصيد بعد الحركة"
    override val orderNewestFirst = "الأحدث أولاً"
    override val orderOldestFirst = "الأقدم أولاً"
    override val viewingWholeShop = "المحل بالكامل"
    override val viewingSpecificCustomer = "زبون محدد"
    override val switchToWholeShop = "عرض المحل بالكامل"
    override val switchCustomer = "تغيير الزبون"
    override val exportAnalysisSummary = "تصدير ملخص التحليل"
    override val sortOrderLabel = "الترتيب الزمني"
    override val homeStatsCredit = "ديون / آجل"
    override val homeStatsCash = "مبيعات كاش"
    override val homeStatsPayments = "سدادات مقبوضة"
    override val homeStatsBalance = "صافي الرصيد"
    override val homeStatsTotal = "إجمالي الحركة"
    override val latestActivities = "آخر النشاطات"
    override val viewFullHistory = "عرض كشف الحساب الكامل"
    override val noActivitiesInPeriod = "لا توجد نشاطات مسجلة خلال هذه الفترة"
    override val noStatsInPeriod = "لا توجد معاملات مالية في هذه الفترة"
    override val recordFirstTransaction = "تسجيل أول معاملة"
    override val notifications = "الإشعارات"
    override val noNotifications = "لا توجد إشعارات جديدة حالياً"
    override val markAllAsRead = "تعيين الكل كمقروء"
    override val clearAllNotifications = "مسح جميع الإشعارات"
    override val setAsHomeCustomer = "تحديد كسياق نشط في الرئيسية"
    override val viewCustomerProfile = "عرض ملف الزبون"
    override val customerContextActive = "الحساب النشط حالياً"

    override val homeTitle = "دفتر الديون والمشتريات"
    override val totalDebtSummary = "إجمالي الديون القائمة"
    override val totalCustomersWithDebt = "زبائن عليهم ديون"
    override val searchCustomerHint = "ابحث بالاسم أو رقم الهاتف..."
    override val noDebtsFound = "لا توجد ديون مستحقة، جميع الحسابات مسددة!"
    override val recordPaymentShort = "تسجيل دفعة"
    override val newPurchaseShort = "تسجيل مشتريات"
    override val quickStatsTodayCredit = "آجل اليوم"
    override val quickStatsTodayPayments = "سداد اليوم"
    override val quickStatsTodayCash = "نقدي اليوم"
    override val customersList = "قائمة الزبائن والديون"
    override val allCustomersSettled = "حساب مسدد بالكامل (0 ₪)"
    override val debtAmount = "المبلغ المتبقي"
    override val outstandingDebt = "الدين المستحق"
    override val noCustomersYet = "لا يوجد زبائن حتى الآن."
    override val noCustomersYetDesc = "أضف زبونك الأول للبدء بتسجيل المشتريات ومتابعة الديون"
    override val noMatchingCustomers = "لم يتم العثور على زبائن مطابقين للبحث"
    override val settledDebt = "مسدد"

    override val purchasesTitle = "تسجيل مشتريات جديدة"
    override val selectCustomer = "اختر الزبون"
    override val purchaseType = "نوع العملية"
    override val creditPurchase = "شراء آجل (دين على الزبون)"
    override val cashPurchase = "شراء نقدي (كاش فوري)"
    override val selectCustomerRequiredForCredit = "يجب اختيار زبون مسجل لعمليات الشراء الآجل"
    override val chooseProducts = "اختر من قائمة الأصناف"
    override val customItem = "صنف مخصص / يدوي"
    override val itemName = "اسم الصنف / المادة"
    override val unitPrice = "سعر الوحدة (₪)"
    override val quantity = "الكمية"
    override val subtotal = "المجموع الفرعي"
    override val addItem = "إضافة إلى الفاتورة"
    override val orderItems = "محتويات الفاتورة"
    override val emptyCart = "لم تتم إضافة أي أصناف بعد. اختر من الأصناف أو أضف يدوياً."
    override val totalAmount = "المجموع الإجمالي"
    override val recordPurchaseButton = "تأكيد وحفظ الفاتورة"
    override val purchaseSuccessMessage = "تم تسجيل الفاتورة بنجاح"
    override val noCustomerSelected = "زبون نقدي عام (بدون تحديد)"
    override val cashCustomerWalkIn = "زبون كاش عابر"
    override val recordTransactionFor = "تسجيل معاملة لـ"
    override val clearCart = "تفريغ السلة"
    override val completeTransaction = "إتمام المعاملة"
    override val cartEmptyWarning = "السلة فارغة. يرجى إضافة منتجات قبل المتابعة."
    override val reviewTransactionTitle = "مراجعة تفاصيل المعاملة"
    override val confirmTransaction = "تأكيد المعاملة"
    override val creditPurchaseNotice = "الشراء الآجل: يزداد به دين الزبون"
    override val cashPurchaseNotice = "الشراء النقدي: مدفوع فوراً ولا يزيد الدين"
    override val searchProductsPlaceholder = "بحث في الأصناف..."
    override val selectCustomerPrompt = "يرجى اختيار العميل أولاً."
    override val changeCustomer = "تغيير العميل"
    override val chooseCustomerDropdown = "[ اختر العميل ▼ ]"

    // Settlement Modes
    override val settlementMode = "طريقة السداد"
    override val settlementFullDebt = "على الحساب (آجل كامل)"
    override val settlementFullDebtDesc = "إضافة كامل المبلغ إلى ذمة الزبون"
    override val settlementFullCash = "دفع كاش كامل"
    override val settlementFullCashDesc = "استلام كامل المبلغ نقداً دون تسجيل أي دين"
    override val settlementPartial = "دفع جزئي (نقد + آجل)"
    override val settlementPartialDesc = "دفع جزء نقداً والباقي يُسجل كدين على الحساب"
    override val paidCashAmount = "المبلغ المدفوع نقداً"
    override val remainingDebtAmount = "المتبقي على الحساب (دين)"
    override val partialPaymentAmountPrompt = "المبلغ المدفوع نقداً (₪) *"
    override val partialPaymentInvalid = "المبلغ المدفوع نقداً يجب أن يكون أكبر من صفر وأقل من الإجمالي"

    // Quick Payment
    override val quickPaymentTitle = "دفع سريع"
    override val quickPayment = "دفع سريع"
    override val quickPaymentSelectCustomer = "اختر الزبون لتسجيل الدفعة"
    override val noDebtForCustomer = "حساب هذا الزبون خالص بالكامل (لا يوجد دين مستحق)"
    override val balanceAfterPayment = "الرصيد بعد السداد"
    override val viewCustomerStatement = "عرض كشف الحساب"
    override val quickPaymentSuccessTitle = "تم تسجيل الدفعة بنجاح!"
    override val paymentSuccessTitle = "تم تسجيل الدفعة بنجاح!"
    override val previousDebtLabel = "الدين السابق"
    override val newDebtLabel = "الدين المتبقي الجديد"

    override val recordPaymentTitle = "تسجيل دفعة سداد دين"
    override val recordPayment = "تسجيل دفعة سداد"
    override val paymentAmount = "المبلغ المدفوع (₪)"
    override val maxPaymentIsDebt = "الحد الأقصى للدفعة هو قيمة الدين القائم"
    override val paymentExceedsDebtError = "لا يمكن أن تتجاوز الدفعة قيمة الدين الحالي"
    override val paymentSuccessMessage = "تم تسجيل الدفعة وخصمها من الدين بنجاح"
    override val payFullDebt = "سداد كامل الدين"

    override val statementsTitle = "كشف الحسابات والسجلات"
    override val filterType = "النوع"
    override val filterStatus = "الحالة"
    override val completedTransactions = "مكتملة"
    override val cancelledTransactions = "ملغاة"
    override val cancelTransaction = "إلغاء المعاملة"
    override val cancelTransactionPrompt = "هل أنت متأكد من إلغاء هذه المعاملة؟ سيتم التراجع عن تأثيرها المالي مع حفظ سجلها."
    override val cancellationReason = "سبب الإلغاء (اختياري)"
    override val transactionCancelledSuccess = "تم إلغاء المعاملة بنجاح"
    override val restoreTransaction = "استعادة المعاملة"
    override val restoreTransactionPrompt = "هل أنت متأكد من استعادة هذه المعاملة؟ سيتم إعادة تأثيرها المالي إلى رصيد الحساب."
    override val transactionRestoredSuccess = "تمت استعادة المعاملة بنجاح"
    override val exportStatementsPdf = "تصدير كشف PDF"
    override val noTransactionsFound = "لا توجد معاملات"
    override val noTransactionsOfType = "لا توجد معاملات من هذا النوع"
    override val noCustomersAvailable = "لا يوجد عملاء متاحين"
    override val noCustomersFoundSearch = "لا يوجد عملاء مطابقون للبحث"
    override val searchCustomerStatementPlaceholder = "ابحث باسم العميل أو رقم الهاتف"
    override val transactionTypeCredit = "شراء بالدين"
    override val transactionTypeCash = "شراء كاش"
    override val transactionTypePayment = "تسديد"
    override val moreDetails = "مزيد من التفاصيل"
    override val transactionDetailsTitle = "تفاصيل المعاملة"
    override val allCustomersMode = "جميع العملاء"
    override val selectCustomerForStatement = "ابحث باسم العميل أو رقم الهاتف"
    override val clearCustomerFilter = "عرض جميع العملاء"
    override val customerLabel = "العميل"
    override val paymentMethod = "طريقة الدفع"
    override val timeLabel = "الوقت"
    override val statusLabel = "الحالة"

    override val customerDetailsTitle = "ملف الزبون"
    override val primaryActionsTitle = "العمليات الأساسية"
    override val recordPurchase = "تسجيل مشتريات"
    override val recordPurchaseDesc = "إضافة فاتورة شراء آجل جديدة لحساب هذا الزبون"
    override val viewAccountStatement = "عرض كشف الحساب"
    override val viewAccountStatementDesc = "استعراض سجل المشتريات والدفعات وتفاصيل الفواتير"
    override val recordPaymentDesc = "سداد دفعة نقدية وخصمها فوراً من الدين القائم"
    override val accountFullyPaid = "الحساب مسدد بالكامل (لا توجد ديون مستحقة)"
    override val confirmAndSave = "تأكيد وحفظ"
    override val amountMustBeGreaterThanZero = "يجب أن يكون المبلغ أكبر من صفر"
    override val customerPhone = "رقم الهاتف"
    override val customerAddress = "العنوان"
    override val customerNotFound = "الزبون غير موجود"
    override val customerNotes = "ملاحظات الزبون"
    override val currentDebt = "الدين الحالي المستحق"
    override val callCustomer = "اتصال"
    override val exportCustomerStatementPdf = "كشف حساب PDF"
    override val customerTransactionHistory = "سجل معاملات الزبون"
    override val customerAccountStatement = "كشف حساب الزبون"
    override val transactionHistory = "سجل المعاملات"
    override val allTransactions = "جميع الحركات"
    override val totalPurchases = "إجمالي المشتريات الآجلة"
    override val totalPaid = "إجمالي الدفعات المسددة"
    override val totalPayments = "إجمالي الدفعات"
    override val payment = "دفعة سداد"
    override val cancelled = "ملغاة"
    override val cashCustomer = "زبون نقدي"
    override val editCustomer = "تعديل البيانات"
    override val archiveCustomer = "أرشفة الزبون"
    override val deleteCustomer = "حذف"
    override val customerArchivedBadge = "مؤرشف"
    override val cannotTransactArchivedCustomer = "هذا الزبون مؤرشف، يرجى استعادته لتسجيل معاملات جديدة."

    override val databaseTitle = "إدارة البيانات والأصناف"
    override val databaseSubtitle = "الزبائن والأصناف والنسخ الاحتياطي والتقارير"
    override val tabCustomers = "الزبائن"
    override val tabProducts = "الأصناف"
    override val tabBackup = "النسخ الاحتياطي"
    override val tabReports = "تقارير PDF"
    override val statusActive = "النشطة"
    override val statusArchived = "المؤرشفة"
    override val statusDeleted = "المحذوفة"
    override val addCustomer = "إضافة زبون جديد"
    override val editCustomerDialogTitle = "تعديل بيانات الزبون"
    override val addProduct = "إضافة صنف جديد"
    override val editProductDialogTitle = "تعديل بيانات الصنف"
    override val customerName = "اسم الزبون"
    override val phoneNumber = "رقم الهاتف"
    override val productName = "اسم الصنف"
    override val defaultPrice = "السعر الافتراضي (₪)"
    override val productUnit = "الوحدة (مثال: حبة، كغم، كرتونة)"
    override val productCategory = "التصنيف (اختياري)"
    override val noCustomersFound = "لا يوجد زبائن مضافون بعد"
    override val noProductsFound = "لا توجد أصناف مضافة بعد"
    override val searchProductHint = "ابحث عن صنف أو تصنيف..."
    override val selectProduct = "اختر الصنف"
    override val editQuantity = "تعديل الكمية"
    override val enterQuantity = "أدخل الكمية المطلوبة"
    override val selectProductImage = "إضافة صورة للصنف"
    override val changeProductImage = "تغيير الصورة"
    override val removeProductImage = "إزالة الصورة"
    override val productImage = "صورة الصنف"
    override val restoreCustomerConfirm = "استعادة هذا الزبون ليصبح نشطاً مجدداً؟"
    override val restoreProductConfirm = "استعادة هذا الصنف ليتاح للبيع مجدداً؟"
    override val permanentDeleteWarning = "تحذير: سيتم حذف السجل نهائياً. لا يمكن التراجع."

    override val backupTitle = "النسخ الاحتياطي والاستعادة"
    override val createBackup = "إنشاء نسخة احتياطية"
    override val restoreBackup = "استعادة نسخة احتياطية"
    override val selectBackupFile = "اختيار ملف نسخة احتياطية"
    override val createBackupButton = "إنشاء ملف نسخة احتياطية"
    override val restoreBackupButton = "استعادة من ملف نسخة احتياطية"
    override val backupDescription = "النسخ الاحتياطي والاستعادة الآمنة"
    override val restoreDescription = "اختر ملف نسخة احتياطية (.shopbackup) لاستعادة كافة البيانات السابقة."
    override val backupCreatedSuccess = "تم إنشاء ملف النسخة الاحتياطية بنجاح"
    override val backupRestoredSuccess = "تمت استعادة البيانات بنجاح"
    override val invalidBackupFile = "الملف المحدد غير صالح كنسخة احتياطية"
    override val restoreWarning = "تنبيه: الاستعادة ستستبدل البيانات الحالية ببيانات ملف النسخة الاحتياطية. هل تود المتابعة؟"
    override val shareBackupFile = "مشاركة / حفظ الملف"

    override val reportsTitle = "مركز تقارير PDF"
    override val generatePdfReports = "إنشاء تقارير PDF الرسمية"
    override val reportCustomerStatement = "كشف حساب زبون تفصيلي"
    override val reportCustomerStatementDesc = "تقرير PDF يتضمن كافة مشتريات ودفعات زبون محدد مع تفاصيل الأصناف والرصيد النهائي."
    override val reportAllCustomersDebts = "كشف ديون جميع الزبائن"
    override val reportAllDebts = "كشف ديون جميع الزبائن"
    override val reportAllDebtsDesc = "تقرير PDF شامل لكافة الزبائن الذين عليهم ديون قائمة وإجمالي مبالغ المحل."
    override val reportProductSales = "تقرير حركة ومبيعات صنف"
    override val reportProductSalesDesc = "تقرير PDF يوضح كمية المبيعات وإيرادات صنف محدد عبر المعاملات."
    override val reportAllProductsCatalog = "دليل أسعار الأصناف"
    override val reportProductCatalog = "قائمة أسعار الأصناف"
    override val reportProductCatalogDesc = "تقرير PDF يحتوي على كامل دليل أصناف المحل وأسعارها ووحداتها."
    override val generatePdf = "إنشاء التقرير PDF"
    override val generatingPdf = "جاري إنشاء ملف PDF..."
    override val pdfGeneratedSuccess = "تم إنشاء التقرير بنجاح، يمكنك عرضه أو مشاركته."
    override val selectProductForReport = "اختر الصنف المطلوب"

    override val settingsTitle = "الإعدادات العامة"
    override val settingsSubtitle = "اللغة وبيانات المحل والعملة"
    override val languageSectionTitle = "لغة التطبيق واتجاه الواجهة"
    override val languageSection = "اللغة والاتجاه"
    override val arabicLanguage = "العربية (RTL)"
    override val englishLanguage = "English (LTR)"
    override val currentLanguage = "اللغة الحالية: العربية (RTL)"
    override val shopInfoSectionTitle = "بيانات المحل التجاري"
    override val shopInfoSection = "بيانات المحل (تظهر في ترويسة تقارير PDF)"
    override val shopName = "اسم المحل"
    override val shopPhone = "هاتف المحل"
    override val shopAddress = "عنوان المحل"
    override val saveShopInfo = "حفظ بيانات المحل"
    override val shopInfoSaved = "تم حفظ بيانات المحل بنجاح"
    override val currencySectionTitle = "العملة الرسمية الثابتة"
    override val currencySection = "العملة الرسمية"
    override val currencyDescription = "الشيكل الإسرائيلي (ILS - ₪)"
    override val fixedCurrencyNote = "العملة الثابتة للتطبيق هي الشيكل الإسرائيلي (ILS - ₪)."
    override val dangerZoneSection = "إدارة البيانات المتقدمة"
    override val factoryReset = "إعادة ضبط المصنع ومسح البيانات"
    override val factoryResetPrompt = "هل أنت متأكد تماماً من مسح كافة بيانات المحل نهائياً؟ يوصى بإنشاء نسخة احتياطية أولاً."
    override val resetAllData = "إعادة ضبط المصنع ومسح البيانات"
    override val resetAllDataConfirm = "هل أنت متأكد تماماً من مسح كافة بيانات المحل نهائياً؟ يوصى بإنشاء نسخة احتياطية أولاً."
    override val aboutSection = "حول التطبيق"
    override val aboutDesc = "تطبيق حسابات وديون المحل التجاري - إصدار احترافي مبسط وسريع."

    override val settingsTabAccount = "الحساب"
    override val settingsTabData = "البيانات"
    override val settingsTabArchive = "الأرشيف"
    override val settingsTabAppearance = "المظهر"
    override val settingsTabReports = "التقارير"
    override val settingsTabAbout = "حول"

    override val ownerManagerName = "اسم المالك / المدير"
    override val storeAddress = "العنوان"
    override val storePhone = "رقم الهاتف"
    override val storeNameRequiredNotice = "اسم المحل مطلوب ويستخدم في ترويسة التقارير الرسمية."

    override val backupAndRestoreTitle = "النسخ الاحتياطي والاستعادة"
    override val createBackupDesc = "إنشاء نسخة احتياطية كاملة لجميع السجلات والزبائن والأصناف والمعاملات."
    override val restoreBackupDesc = "استعادة قاعدة البيانات من ملف نسخة احتياطية سابق تم حفظه."
    override val backupInspectTitle = "معاينة ملف النسخة الاحتياطية"
    override val backupInspectDate = "تاريخ النسخة:"
    override val backupInspectCustomers = "عدد الزبائن:"
    override val backupInspectProducts = "عدد الأصناف:"
    override val backupInspectTransactions = "عدد المعاملات:"
    override val confirmRestoreAction = "استعادة البيانات الآن"
    override val restoreSafetyNotice = "سيتم إنشاء نسخة أمان احتياطية تلقائياً قبل تنفيذ الاستعادة."

    override val archiveSectionCustomers = "الزبائن"
    override val archiveSectionProducts = "الأصناف"
    override val archiveSectionTransactions = "المعاملات الملغاة"
    override val cancelledTxNotice = "المعاملات الملغاة يتم الاحتفاظ بها كأرشيف تاريخي غير قابل للتعديل للحفاظ على الدقة المحاسبية."
    override val permanentDeleteConfirmPrompt = "تحذير: سيتم حذف هذا السجل نهائياً ولن يمكن استرجاعه. هل تريد المتابعة؟"

    override val themeTitle = "وضع العرض"
    override val themeLight = "فاتح"
    override val themeDark = "داكن"
    override val themeSystem = "تلقائي (حسب النظام)"
    override val themeLightDesc = "مظهر ناصع ومريح للإضاءة العالية"
    override val themeDarkDesc = "مظهر داكن مريح للعين وموفر للطاقة"
    override val themeSystemDesc = "يتبع تلقائياً إعدادات نظام جهازك"
    override val displayModeTitle = "وضع العرض (الإضاءة)"
    override val themesTitle = "السمات (الهوية البصرية)"
    override val themesSubtitle = "اختر الطابع اللوني والتصميمي المفضل للتطبيق"
    override val themePurple = "بنفسجي"
    override val themeGold = "ذهبي"
    override val themeBlackAndWhite = "أبيض وأسود"
    override val themePurpleDesc = "طابع عصري حديث بلمسات بنفسجية حيوية"
    override val themeGoldDesc = "طابع راقٍ وهادئ بلمسات ذهبية دافئة"
    override val themeBlackAndWhiteDesc = "طابع محاسبي عملي سريع بدون مؤثرات حركية"

    override val appVersionLabel = "الإصدار:"
    override val appVersionValue = "v1.0.0"
    override val developerLabel = "المطور:"
    override val developerValue = "Google AI Studio Developer"
    override val privacyPolicyTitle = "سياسة الخصوصية"
    override val privacyPolicyContent = "يعمل التطبيق بشكل محلي 100% دون الاتصال بأي خوادم خارجية. بياناتك ومبيعاتك محفوظة بأمان على جهازك فقط."
    override val termsTitle = "شروط الاستخدام"
    override val termsContent = "التطبيق مخصص لإدارة حسابات ومشتريات المحلات التجارية المحلية بدقة وموثوقية عالية."
    override val contactDevTitle = "تواصل مع المطور"
    override val contactDevContent = "لتقديم الاقتراحات أو الإبلاغ عن المشاكل، يرجى التواصل عبر قنوات المطور الرسمية."

    override val editProduct = "تعديل بيانات الصنف"
    override val archiveProduct = "أرشفة الصنف"
    override val deleteProduct = "حذف الصنف"
    override val appNameFull = "حسابات وديون المحل"
    override val close = "إغلاق"
    override val noResultsMessage = "لا توجد نتائج تطابق بحثك"
    override val storeDetailsInfoNotice = "بيانات المحل تُعرض تلقائياً في ترويسة تقارير PDF الرسمية وكشوف الحساب."
    override val storeInfoTitle = "بيانات المحل وهوية المتجر"
    override val storeName = "اسم المحل"
    override val ownerName = "اسم صاحب المحل / المدير"
    override val saveStoreDetails = "حفظ بيانات المحل"
    override val dangerZoneTitle = "منطقة الخطر"
    override val resetAllDataDesc = "إعادة ضبط المصنع ومسح كافة السجلات والديون والزبائن والمعاملات."
    override val confirmRestoreTitle = "تأكيد استعادة النسخة الاحتياطية"
    override val restoreInspectNotice = "تم فحص ملف النسخة الاحتياطية بنجاح. يرجى مراجعة تفاصيل البيانات قبل المتابعة:"
    override val backupDateLabel = "تاريخ النسخة"
    override val confirmAndRestore = "تأكيد واستعادة البيانات"
    override val resetAllDataWarning = "تحذير شديد: سيتم مسح كافة الزبائن والأصناف وسجلات الديون نهائياً. لا يمكن التراجع عن هذه الخطوة!"
    override val noArchiveRecords = "لا توجد سجلات في الأرشيف"
    override val noArchiveCustomersDesc = "لم يتم أرشفة أو حذف أي زبائن حتى الآن."
    override val noArchiveProductsDesc = "لم يتم أرشفة أو حذف أي أصناف حتى الآن."
    override val noCancelledTransactionsNotice = "لا توجد معاملات ملغاة حالياً في السجل."
    override val pdfReportsCenterNotice = "مركز التقارير المحاسبية: يمكنك تصدير كشوف الحساب وتقارير الديون بصيغة PDF قابلة للطباعة والمشاركة."
    override val termsOfUseTitle = "شروط الاستخدام"
    override val termsOfUseContent = "التطبيق مخصص لإدارة حسابات ومشتريات المحلات التجارية المحلية بدقة وموثوقية عالية."
    override val contactDeveloperTitle = "تواصل مع المطور"
    override val contactDeveloperContent = "لتقديم الاقتراحات أو الاستفسارات، يرجى التواصل مع فريق التطوير."
    override val developerInfo = "Google AI Studio Developer"
    override val languageTitle = "لغة التطبيق"
    override val unknownCustomer = "زبون غير محدد"
}

object EnglishStrings : Strings {
    override val appName = "Shop Accounts"
    override val customer = "Customer"
    override val currencySymbol = "₪"
    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val save = "Save"
    override val delete = "Delete"
    override val edit = "Edit"
    override val archive = "Archive"
    override val restore = "Restore"
    override val search = "Search..."
    override val all = "All"
    override val active = "Active"
    override val archived = "Archived"
    override val deleted = "Deleted"
    override val loading = "Loading..."
    override val emptyState = "No data available currently"
    override val errorOccurred = "An error occurred, please try again"
    override val success = "Operation completed successfully"
    override val optional = "Optional"
    override val required = "Required"
    override val notes = "Notes"
    override val date = "Date"
    override val status = "Status"
    override val actions = "Actions"
    override val details = "Details"
    override val share = "Share"
    override val print = "Print"
    override val exportPdf = "Export PDF"
    override val generateReport = "Generate PDF Report"

    override val navHome = "Home"
    override val navAccounts = "Accounts"
    override val navPurchases = "Purchases"
    override val navStatements = "Statements"
    override val navDatabase = "Database"
    override val navSettings = "Settings"
    override val navAnalysisCenter = "Analysis Center"
    override val navMore = "More"

    override val drawerSectionMain = "Main"
    override val drawerSectionAnalysis = "Analysis"
    override val drawerSectionManagement = "Management"
    override val drawerHome = "Home"
    override val drawerAccounts = "Accounts"
    override val drawerPurchases = "Purchases"
    override val drawerAnalysis = "Analysis Center"
    override val drawerMore = "More"
    override val drawerStats = "Statistics"
    override val drawerStatements = "Account Statement"
    override val drawerReports = "Reports"
    override val drawerShopInfo = "Shop Information"
    override val drawerAppSettings = "App Settings"
    override val drawerDataCenter = "Data Center"
    override val drawerAboutApp = "About App"

    override val quickActionTitle = "Quick Action"
    override val quickActionNewPurchase = "Record Transaction"
    override val quickActionNewPurchaseDesc = "New sale, credit or cash invoice"
    override val quickActionQuickPayment = "Quick Payment"
    override val quickActionQuickPaymentDesc = "Record customer cash debt payment"
    override val selectCustomerForPayment = "Select Customer for Payment"

    override val homeShopMode = "Shop-Wide Overview"
    override val homeShopModeDesc = "Complete shop metrics and all customer activities"
    override val homeCustomerMode = "Customer Account Context"
    override val clearCustomerSelection = "Clear Selection"
    override val periodToday = "Today"
    override val periodThisWeek = "This Week"
    override val periodThisMonth = "This Month"
    override val periodCustom = "Custom Period"
    override val periodAllTime = "All Time"
    override val selectDateRange = "Select Date Range"
    override val startDateLabel = "Start Date"
    override val endDateLabel = "End Date"
    override val tabAnalysisCenter = "Analysis & Statistics"
    override val tabAccountStatement = "Account Statement"
    override val sectionOverview = "Overview"
    override val sectionFinancialStats = "Financial Statistics"
    override val sectionCustomerDebt = "Customer Debt"
    override val sectionCashPayments = "Cash & Collections"
    override val sectionTransactions = "Transactions Activity"
    override val sectionPeriodAnalysis = "Period Analysis"
    override val totalSalesLabel = "Total Sales"
    override val cashSalesLabel = "Cash Sales"
    override val creditSalesLabel = "Credit Sales"
    override val paymentsCollectedLabel = "Collected Payments"
    override val totalCustomersLabel = "Total Customers"
    override val activeCustomersLabel = "Active Customers"
    override val collectionRateLabel = "Collection Rate"
    override val averageDebtLabel = "Average Debt"
    override val breakdownTitle = "Operations Breakdown"
    override val salesVsPaymentsTitle = "Sales vs Payments"
    override val debtOverviewTitle = "Debt Overview"
    override val noChartData = "No transactions recorded for this period"
    override val runningBalanceLabel = "Running Balance"
    override val orderNewestFirst = "Newest First"
    override val orderOldestFirst = "Oldest First"
    override val viewingWholeShop = "Whole Shop"
    override val viewingSpecificCustomer = "Specific Customer"
    override val switchToWholeShop = "View Whole Shop"
    override val switchCustomer = "Change Customer"
    override val exportAnalysisSummary = "Export Summary"
    override val sortOrderLabel = "Sort Order"
    override val homeStatsCredit = "Credit / Debts"
    override val homeStatsCash = "Cash Sales"
    override val homeStatsPayments = "Collected Payments"
    override val homeStatsBalance = "Net Balance"
    override val homeStatsTotal = "Total Volume"
    override val latestActivities = "Latest Activities"
    override val viewFullHistory = "View Full Account Statement"
    override val noActivitiesInPeriod = "No activities recorded in this period"
    override val noStatsInPeriod = "No financial transactions in this period"
    override val recordFirstTransaction = "Record First Transaction"
    override val notifications = "Notifications"
    override val noNotifications = "No new notifications at this time"
    override val markAllAsRead = "Mark All as Read"
    override val clearAllNotifications = "Clear All Notifications"
    override val setAsHomeCustomer = "Set as Active Context on Home"
    override val viewCustomerProfile = "View Customer Profile"
    override val customerContextActive = "Currently Active Customer"

    override val homeTitle = "Shop Debt & Purchases"
    override val totalDebtSummary = "Total Outstanding Debt"
    override val totalCustomersWithDebt = "Customers with Debt"
    override val searchCustomerHint = "Search by name or phone..."
    override val noDebtsFound = "No debts pending, all customer accounts are settled!"
    override val recordPaymentShort = "Record Payment"
    override val newPurchaseShort = "New Purchase"
    override val quickStatsTodayCredit = "Today's Credit"
    override val quickStatsTodayPayments = "Today's Payments"
    override val quickStatsTodayCash = "Today's Cash"
    override val customersList = "Customers & Debts"
    override val allCustomersSettled = "Account Settled (0 ₪)"
    override val debtAmount = "Remaining Debt"
    override val outstandingDebt = "Outstanding Debt"
    override val noCustomersYet = "No customers yet."
    override val noCustomersYetDesc = "Add your first customer to track credit purchases and debts"
    override val noMatchingCustomers = "No matching customers found"
    override val settledDebt = "Settled"

    override val purchasesTitle = "Record New Purchase"
    override val selectCustomer = "Select Customer"
    override val purchaseType = "Transaction Type"
    override val creditPurchase = "Credit Purchase (On Account)"
    override val cashPurchase = "Cash Purchase (Immediate)"
    override val selectCustomerRequiredForCredit = "Customer selection is required for credit purchases"
    override val chooseProducts = "Pick from Products"
    override val customItem = "Custom / Ad-hoc Item"
    override val itemName = "Item Name"
    override val unitPrice = "Unit Price (₪)"
    override val quantity = "Quantity"
    override val subtotal = "Subtotal"
    override val addItem = "Add to Cart"
    override val orderItems = "Cart Items"
    override val emptyCart = "No items added yet. Pick products or add a custom item."
    override val totalAmount = "Total Amount"
    override val recordPurchaseButton = "Confirm & Save Purchase"
    override val purchaseSuccessMessage = "Purchase recorded successfully"
    override val noCustomerSelected = "General Walk-in Customer"
    override val cashCustomerWalkIn = "Cash Walk-in Customer"
    override val recordTransactionFor = "Record Transaction for"
    override val clearCart = "Clear Cart"
    override val completeTransaction = "Complete Transaction"
    override val cartEmptyWarning = "The cart is empty. Add products before continuing."
    override val reviewTransactionTitle = "Review Transaction"
    override val confirmTransaction = "Confirm Transaction"
    override val creditPurchaseNotice = "Credit Purchase: The total increases customer debt."
    override val cashPurchaseNotice = "Cash Purchase: Recorded as paid immediately and does not increase debt."
    override val searchProductsPlaceholder = "Search products..."
    override val selectCustomerPrompt = "Please select a customer first."
    override val changeCustomer = "Change Customer"
    override val chooseCustomerDropdown = "[ Select Customer ▼ ]"

    // Settlement Modes
    override val settlementMode = "Settlement Method"
    override val settlementFullDebt = "On Account (Full Debt)"
    override val settlementFullDebtDesc = "Add entire amount to customer's outstanding balance"
    override val settlementFullCash = "Full Cash Payment"
    override val settlementFullCashDesc = "Receive entire amount in cash without creating debt"
    override val settlementPartial = "Partial Payment (Cash + Debt)"
    override val settlementPartialDesc = "Pay part in cash, add remaining balance to debt"
    override val paidCashAmount = "Cash Paid Amount"
    override val remainingDebtAmount = "Remaining Debt (On Account)"
    override val partialPaymentAmountPrompt = "Cash Paid Amount (₪) *"
    override val partialPaymentInvalid = "Cash payment must be greater than zero and less than total amount"

    // Quick Payment
    override val quickPaymentTitle = "Quick Payment"
    override val quickPayment = "Quick Payment"
    override val quickPaymentSelectCustomer = "Select Customer for Payment"
    override val noDebtForCustomer = "Account is fully settled (no outstanding debt)"
    override val balanceAfterPayment = "Balance After Payment"
    override val viewCustomerStatement = "View Account Statement"
    override val quickPaymentSuccessTitle = "Payment Recorded Successfully!"
    override val paymentSuccessTitle = "Payment Recorded Successfully!"
    override val previousDebtLabel = "Previous Debt"
    override val newDebtLabel = "New Remaining Debt"

    override val recordPaymentTitle = "Record Customer Payment"
    override val recordPayment = "Record Payment"
    override val paymentAmount = "Payment Amount (₪)"
    override val maxPaymentIsDebt = "Maximum payment is the current debt"
    override val paymentExceedsDebtError = "Payment cannot exceed outstanding debt"
    override val paymentSuccessMessage = "Payment recorded and deducted from debt"
    override val payFullDebt = "Pay Full Debt"

    override val statementsTitle = "Account Statements & Records"
    override val filterType = "Type"
    override val filterStatus = "Status"
    override val completedTransactions = "Completed"
    override val cancelledTransactions = "Cancelled"
    override val cancelTransaction = "Cancel Transaction"
    override val cancelTransactionPrompt = "Are you sure you want to cancel this transaction? Financial impact will be reversed while keeping the record."
    override val cancellationReason = "Reason for cancellation (optional)"
    override val transactionCancelledSuccess = "Transaction cancelled successfully"
    override val restoreTransaction = "Restore Transaction"
    override val restoreTransactionPrompt = "Are you sure you want to restore this transaction? Its financial effect will be reapplied to the account balance."
    override val transactionRestoredSuccess = "Transaction restored successfully"
    override val exportStatementsPdf = "Export PDF Statement"
    override val noTransactionsFound = "No transactions"
    override val noTransactionsOfType = "No transactions of this type"
    override val noCustomersAvailable = "No customers available."
    override val noCustomersFoundSearch = "No customers match your search"
    override val searchCustomerStatementPlaceholder = "Search by customer name or phone"
    override val transactionTypeCredit = "Credit Purchase"
    override val transactionTypeCash = "Cash Purchase"
    override val transactionTypePayment = "Payment"
    override val moreDetails = "More Details"
    override val transactionDetailsTitle = "Transaction Details"
    override val allCustomersMode = "All Customers"
    override val selectCustomerForStatement = "Search by customer name or phone"
    override val clearCustomerFilter = "Show All Customers"
    override val customerLabel = "Customer"
    override val paymentMethod = "Payment Method"
    override val timeLabel = "Time"
    override val statusLabel = "Status"

    override val customerDetailsTitle = "Customer Profile"
    override val primaryActionsTitle = "Primary Actions"
    override val recordPurchase = "Record Purchase"
    override val recordPurchaseDesc = "Add a new credit purchase invoice for this customer"
    override val viewAccountStatement = "View Account Statement"
    override val viewAccountStatementDesc = "Review transaction history, payments, and invoice items"
    override val recordPaymentDesc = "Record a payment and immediately deduct it from debt"
    override val accountFullyPaid = "Account is fully settled (no outstanding debt)"
    override val confirmAndSave = "Confirm & Save"
    override val amountMustBeGreaterThanZero = "Amount must be greater than zero"
    override val customerPhone = "Phone Number"
    override val customerAddress = "Address"
    override val customerNotFound = "Customer not found"
    override val customerNotes = "Customer Notes"
    override val currentDebt = "Current Outstanding Debt"
    override val callCustomer = "Call"
    override val exportCustomerStatementPdf = "PDF Statement"
    override val customerTransactionHistory = "Transaction History"
    override val customerAccountStatement = "Customer Account Statement"
    override val transactionHistory = "Transaction History"
    override val allTransactions = "All Transactions"
    override val totalPurchases = "Total Credit Purchases"
    override val totalPaid = "Total Payments"
    override val totalPayments = "Total Payments"
    override val payment = "Payment"
    override val cancelled = "Cancelled"
    override val cashCustomer = "Cash Customer"
    override val editCustomer = "Edit Info"
    override val archiveCustomer = "Archive Customer"
    override val deleteCustomer = "Delete"
    override val customerArchivedBadge = "Archived"
    override val cannotTransactArchivedCustomer = "This customer is archived. Restore to record new transactions."

    override val databaseTitle = "Data & Products Management"
    override val databaseSubtitle = "Customers, Products, Backup & Reports"
    override val tabCustomers = "Customers"
    override val tabProducts = "Products"
    override val tabBackup = "Backup & Restore"
    override val tabReports = "PDF Reports"
    override val statusActive = "Active"
    override val statusArchived = "Archived"
    override val statusDeleted = "Deleted"
    override val addCustomer = "Add New Customer"
    override val editCustomerDialogTitle = "Edit Customer Info"
    override val addProduct = "Add New Product"
    override val editProductDialogTitle = "Edit Product Info"
    override val customerName = "Customer Name"
    override val phoneNumber = "Phone Number"
    override val productName = "Product Name"
    override val defaultPrice = "Default Price (₪)"
    override val productUnit = "Unit (e.g. pcs, kg, box)"
    override val productCategory = "Category (Optional)"
    override val noCustomersFound = "No customers added yet"
    override val noProductsFound = "No products added yet"
    override val searchProductHint = "Search product or category..."
    override val selectProduct = "Select Product"
    override val editQuantity = "Edit Quantity"
    override val enterQuantity = "Enter required quantity"
    override val selectProductImage = "Add Product Image"
    override val changeProductImage = "Change Image"
    override val removeProductImage = "Remove Image"
    override val productImage = "Product Image"
    override val restoreCustomerConfirm = "Restore this customer to active status?"
    override val restoreProductConfirm = "Restore this product to active catalog?"
    override val permanentDeleteWarning = "Warning: Record will be permanently removed."

    override val backupTitle = "Backup & Restore"
    override val createBackup = "Create Backup"
    override val restoreBackup = "Restore Backup"
    override val selectBackupFile = "Select Backup File"
    override val createBackupButton = "Create Real Backup File"
    override val restoreBackupButton = "Restore from Backup File"
    override val backupDescription = "Secure Backup & Restore"
    override val restoreDescription = "Select a backup file (.shopbackup) to restore all records."
    override val backupCreatedSuccess = "Backup file created successfully"
    override val backupRestoredSuccess = "Data restored successfully from backup"
    override val invalidBackupFile = "Selected file is invalid or corrupted"
    override val restoreWarning = "Warning: Restoring will overwrite existing data with the backup file data. Continue?"
    override val shareBackupFile = "Share / Save File"

    override val reportsTitle = "PDF Reports Hub"
    override val generatePdfReports = "Generate Official PDF Reports"
    override val reportCustomerStatement = "Customer Statement PDF"
    override val reportCustomerStatementDesc = "Detailed PDF containing all purchases and payments for a specific customer with item snapshots."
    override val reportAllCustomersDebts = "All Customers Debt Report"
    override val reportAllDebts = "All Customers Debt Report"
    override val reportAllDebtsDesc = "Comprehensive PDF summarizing all customers with outstanding debt and total store receivables."
    override val reportProductSales = "Product Movement & Sales"
    override val reportProductSalesDesc = "PDF report detailing sales volume, units sold, and revenue for a selected product."
    override val reportAllProductsCatalog = "Products Price Catalog"
    override val reportProductCatalog = "Product Catalog & Price List"
    override val reportProductCatalogDesc = "Full store product directory PDF with prices, units, and categories."
    override val generatePdf = "Generate PDF"
    override val generatingPdf = "Generating PDF report..."
    override val pdfGeneratedSuccess = "PDF report generated successfully. Ready to view or share."
    override val selectProductForReport = "Select Target Product"

    override val settingsTitle = "General Settings"
    override val settingsSubtitle = "Language, Shop Info & Currency"
    override val languageSectionTitle = "App Language & Direction"
    override val languageSection = "Language & Direction"
    override val arabicLanguage = "العربية (RTL)"
    override val englishLanguage = "English (LTR)"
    override val currentLanguage = "Current Language: English (LTR)"
    override val shopInfoSectionTitle = "Shop Details"
    override val shopInfoSection = "Shop Information (Appears in PDF Headers)"
    override val shopName = "Shop Name"
    override val shopPhone = "Shop Phone"
    override val shopAddress = "Shop Address"
    override val saveShopInfo = "Save Shop Details"
    override val shopInfoSaved = "Shop details saved successfully"
    override val currencySectionTitle = "Fixed Official Currency"
    override val currencySection = "Official Currency"
    override val currencyDescription = "Israeli Shekel (ILS - ₪)"
    override val fixedCurrencyNote = "The fixed currency for this application is Israeli Shekel (ILS - ₪)."
    override val dangerZoneSection = "Advanced Data Management"
    override val factoryReset = "Factory Reset & Wipe Data"
    override val factoryResetPrompt = "Are you completely sure you want to permanently erase all data? Creating a backup first is strongly advised."
    override val resetAllData = "Factory Reset & Wipe All Data"
    override val resetAllDataConfirm = "Are you completely sure you want to permanently erase all data? Creating a backup first is strongly advised."
    override val aboutSection = "About Application"
    override val aboutDesc = "Shop Debt & Purchases Manager - Fast, simple, and reliable local shop bookkeeping."

    override val settingsTabAccount = "Account"
    override val settingsTabData = "Data"
    override val settingsTabArchive = "Archive"
    override val settingsTabAppearance = "Appearance"
    override val settingsTabReports = "Reports"
    override val settingsTabAbout = "About"

    override val ownerManagerName = "Owner / Manager Name"
    override val storeAddress = "Store Address"
    override val storePhone = "Store Phone"
    override val storeNameRequiredNotice = "Store name is required and appears on official PDF report headers."

    override val backupAndRestoreTitle = "Backup & Restore"
    override val createBackupDesc = "Create a complete snapshot of all customers, products, debts, and transactions."
    override val restoreBackupDesc = "Restore database from a previously saved JSON backup file."
    override val backupInspectTitle = "Backup File Inspection"
    override val backupInspectDate = "Backup Date:"
    override val backupInspectCustomers = "Customer Count:"
    override val backupInspectProducts = "Product Count:"
    override val backupInspectTransactions = "Transaction Count:"
    override val confirmRestoreAction = "Restore Data Now"
    override val restoreSafetyNotice = "A safety backup will be created automatically before proceeding with the restore."

    override val archiveSectionCustomers = "Customers"
    override val archiveSectionProducts = "Products"
    override val archiveSectionTransactions = "Cancelled Transactions"
    override val cancelledTxNotice = "Cancelled transactions are retained as an immutable historical audit trail to ensure accounting integrity."
    override val permanentDeleteConfirmPrompt = "Warning: This record will be permanently deleted and cannot be recovered. Do you wish to proceed?"

    override val themeTitle = "Display Mode"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val themeSystem = "System Default"
    override val themeLightDesc = "Bright and clean layout for daylight use"
    override val themeDarkDesc = "Dark layout that is easy on the eyes and saves battery"
    override val themeSystemDesc = "Automatically matches your device system theme"
    override val displayModeTitle = "Display Mode"
    override val themesTitle = "Themes"
    override val themesSubtitle = "Choose the visual identity and color scheme for the application"
    override val themePurple = "Purple"
    override val themeGold = "Gold"
    override val themeBlackAndWhite = "Black & White"
    override val themePurpleDesc = "Modern energetic violet & purple visual theme"
    override val themeGoldDesc = "Warm, elegant & luxurious gold visual theme"
    override val themeBlackAndWhiteDesc = "Pure monochrome focused interface with static performance"

    override val appVersionLabel = "Version:"
    override val appVersionValue = "v1.0.0"
    override val developerLabel = "Developer:"
    override val developerValue = "Google AI Studio Developer"
    override val privacyPolicyTitle = "Privacy Policy"
    override val privacyPolicyContent = "The app operates 100% locally on your device without sending any data to external servers. All debts and transactions remain completely private."
    override val termsTitle = "Terms of Use"
    override val termsContent = "Designed for local shopkeepers to manage customer debts, transactions, and statements reliably and efficiently."
    override val contactDevTitle = "Contact Developer"
    override val contactDevContent = "For support, feedback, or feature requests, contact through official developer channels."

    override val editProduct = "Edit Product"
    override val archiveProduct = "Archive Product"
    override val deleteProduct = "Delete Product"
    override val appNameFull = "Shop Debt & Purchases Manager"
    override val close = "Close"
    override val noResultsMessage = "No results match your search"
    override val storeDetailsInfoNotice = "Store information is automatically displayed on official PDF report headers and statements."
    override val storeInfoTitle = "Store Information & Identity"
    override val storeName = "Store Name"
    override val ownerName = "Owner / Manager Name"
    override val saveStoreDetails = "Save Store Details"
    override val dangerZoneTitle = "Danger Zone"
    override val resetAllDataDesc = "Factory reset and erase all records, debts, customers, and transactions."
    override val confirmRestoreTitle = "Confirm Backup Restoration"
    override val restoreInspectNotice = "Backup file inspected successfully. Please review the dataset details before proceeding:"
    override val backupDateLabel = "Backup Date"
    override val confirmAndRestore = "Confirm & Restore Data"
    override val resetAllDataWarning = "Severe Warning: All customer profiles, products, and debt histories will be permanently wiped. This action cannot be undone!"
    override val noArchiveRecords = "No Archived Records"
    override val noArchiveCustomersDesc = "No customers are currently archived or deleted."
    override val noArchiveProductsDesc = "No products are currently archived or deleted."
    override val noCancelledTransactionsNotice = "No cancelled transactions in the audit ledger."
    override val pdfReportsCenterNotice = "Accounting Reports Center: Export print-ready customer statements, debt lists, and catalogs in PDF format."
    override val termsOfUseTitle = "Terms of Use"
    override val termsOfUseContent = "Designed for local shopkeepers to manage customer debts, transactions, and statements reliably and efficiently."
    override val contactDeveloperTitle = "Contact Developer"
    override val contactDeveloperContent = "For feedback, inquiries, or support, please reach out to the development team."
    override val developerInfo = "Google AI Studio Developer"
    override val languageTitle = "Application Language"
    override val unknownCustomer = "Unknown Customer"
}

fun formatShekels(amount: Double): String {
    val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    return "₪ " + df.format(amount)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.US)
    return sdf.format(Date(timestamp))
}

fun formatShortDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    return sdf.format(Date(timestamp))
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd | HH:mm", Locale.US)
    return sdf.format(Date(timestamp))
}

fun formatDateOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    return sdf.format(Date(timestamp))
}

fun formatTimeOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    return sdf.format(Date(timestamp))
}
