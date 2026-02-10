package com.example.finvovo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight

data class CurrencyItem(val country: String, val code: String, val symbol: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    currentCurrencySymbol: String,
    onCurrencySelected: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedSymbol by remember { mutableStateOf(currentCurrencySymbol) }

    // A sample list of currencies. In a real app, this might come from a comprehensive data source.
    val currencies = remember {
        listOf(
            CurrencyItem("Afghanistan", "AFN", "؋"),
            CurrencyItem("Albania", "ALL", "Lek"),
            CurrencyItem("Algeria", "DZD", "د.ج"),
            CurrencyItem("Andorra", "EUR", "€"),
            CurrencyItem("Angola", "AOA", "Kz"),
            CurrencyItem("Antigua and Barbuda", "XCD", "$"),
            CurrencyItem("Argentina", "ARS", "$"),
            CurrencyItem("Armenia", "AMD", "֏"),
            CurrencyItem("Australia", "AUD", "$"),
            CurrencyItem("Austria", "EUR", "€"),
            CurrencyItem("Azerbaijan", "AZN", "₼"),
            CurrencyItem("Bahamas", "BSD", "$"),
            CurrencyItem("Bahrain", "BHD", ".د.ب"),
            CurrencyItem("Bangladesh", "BDT", "৳"),
            CurrencyItem("Barbados", "BBD", "$"),
            CurrencyItem("Belarus", "BYN", "Br"),
            CurrencyItem("Belgium", "EUR", "€"),
            CurrencyItem("Belize", "BZD", "$"),
            CurrencyItem("Benin", "XOF", "CFA"),
            CurrencyItem("Bermuda", "BMD", "$"),
            CurrencyItem("Bhutan", "BTN", "Nu."),
            CurrencyItem("Bolivia", "BOB", "Bs."),
            CurrencyItem("Bosnia and Herzegovina", "BAM", "KM"),
            CurrencyItem("Botswana", "BWP", "P"),
            CurrencyItem("Brazil", "BRL", "R$"),
            CurrencyItem("Brunei", "BND", "$"),
            CurrencyItem("Bulgaria", "BGN", "лв"),
            CurrencyItem("Burkina Faso", "XOF", "CFA"),
            CurrencyItem("Burundi", "BIF", "Fr"),
            CurrencyItem("Cambodia", "KHR", "៛"),
            CurrencyItem("Cameroon", "XAF", "FCFA"),
            CurrencyItem("Canada", "CAD", "$"),
            CurrencyItem("Cape Verde", "CVE", "Esc"),
            CurrencyItem("Central African Republic", "XAF", "FCFA"),
            CurrencyItem("Chad", "XAF", "FCFA"),
            CurrencyItem("Chile", "CLP", "$"),
            CurrencyItem("China", "CNY", "¥"),
            CurrencyItem("Colombia", "COP", "$"),
            CurrencyItem("Comoros", "KMF", "CF"),
            CurrencyItem("Congo", "XAF", "FCFA"),
            CurrencyItem("Costa Rica", "CRC", "₡"),
            CurrencyItem("Croatia", "EUR", "€"),
            CurrencyItem("Cuba", "CUP", "$"),
            CurrencyItem("Cyprus", "EUR", "€"),
            CurrencyItem("Czech Republic", "CZK", "Kč"),
            CurrencyItem("Denmark", "DKK", "kr"),
            CurrencyItem("Djibouti", "DJF", "Fdj"),
            CurrencyItem("Dominica", "XCD", "$"),
            CurrencyItem("Dominican Republic", "DOP", "$"),
            CurrencyItem("Ecuador", "USD", "$"),
            CurrencyItem("Egypt", "EGP", "£"),
            CurrencyItem("El Salvador", "USD", "$"),
            CurrencyItem("Equatorial Guinea", "XAF", "FCFA"),
            CurrencyItem("Eritrea", "ERN", "Nfk"),
            CurrencyItem("Estonia", "EUR", "€"),
            CurrencyItem("Ethiopia", "ETB", "Br"),
            CurrencyItem("Fiji", "FJD", "$"),
            CurrencyItem("Finland", "EUR", "€"),
            CurrencyItem("France", "EUR", "€"),
            CurrencyItem("Gabon", "XAF", "FCFA"),
            CurrencyItem("Gambia", "GMD", "D"),
            CurrencyItem("Georgia", "GEL", "₾"),
            CurrencyItem("Germany", "EUR", "€"),
            CurrencyItem("Ghana", "GHS", "₵"),
            CurrencyItem("Greece", "EUR", "€"),
            CurrencyItem("Grenada", "XCD", "$"),
            CurrencyItem("Guatemala", "GTQ", "Q"),
            CurrencyItem("Guinea", "GNF", "Fr"),
            CurrencyItem("Guinea-Bissau", "XOF", "CFA"),
            CurrencyItem("Guyana", "GYD", "$"),
            CurrencyItem("Haiti", "HTG", "G"),
            CurrencyItem("Honduras", "HNL", "L"),
            CurrencyItem("Hong Kong", "HKD", "$"),
            CurrencyItem("Hungary", "HUF", "Ft"),
            CurrencyItem("Iceland", "ISK", "kr"),
            CurrencyItem("India", "INR", "₹"),
            CurrencyItem("Indonesia", "IDR", "Rp"),
            CurrencyItem("Iran", "IRR", "﷼"),
            CurrencyItem("Iraq", "IQD", "ع.د"),
            CurrencyItem("Ireland", "EUR", "€"),
            CurrencyItem("Israel", "ILS", "₪"),
            CurrencyItem("Italy", "EUR", "€"),
            CurrencyItem("Jamaica", "JMD", "$"),
            CurrencyItem("Japan", "JPY", "¥"),
            CurrencyItem("Jordan", "JOD", "د.ا"),
            CurrencyItem("Kazakhstan", "KZT", "₸"),
            CurrencyItem("Kenya", "KES", "Sh"),
            CurrencyItem("Kiribati", "AUD", "$"),
            CurrencyItem("Korea, North", "KPW", "₩"),
            CurrencyItem("Korea, South", "KRW", "₩"),
            CurrencyItem("Kuwait", "KWD", "د.ك"),
            CurrencyItem("Kyrgyzstan", "KGS", "с"),
            CurrencyItem("Laos", "LAK", "₭"),
            CurrencyItem("Latvia", "EUR", "€"),
            CurrencyItem("Lebanon", "LBP", "ل.ل"),
            CurrencyItem("Lesotho", "LSL", "L"),
            CurrencyItem("Liberia", "LRD", "$"),
            CurrencyItem("Libya", "LYD", "ل.د"),
            CurrencyItem("Liechtenstein", "CHF", "Fr"),
            CurrencyItem("Lithuania", "EUR", "€"),
            CurrencyItem("Luxembourg", "EUR", "€"),
            CurrencyItem("Madagascar", "MGA", "Ar"),
            CurrencyItem("Malawi", "MWK", "MK"),
            CurrencyItem("Malaysia", "MYR", "RM"),
            CurrencyItem("Maldives", "MVR", ".ރ"),
            CurrencyItem("Mali", "XOF", "CFA"),
            CurrencyItem("Malta", "EUR", "€"),
            CurrencyItem("Marshall Islands", "USD", "$"),
            CurrencyItem("Mauritania", "MRU", "UM"),
            CurrencyItem("Mauritius", "MUR", "₨"),
            CurrencyItem("Mexico", "MXN", "$"),
            CurrencyItem("Micronesia", "USD", "$"),
            CurrencyItem("Moldova", "MDL", "L"),
            CurrencyItem("Monaco", "EUR", "€"),
            CurrencyItem("Mongolia", "MNT", "₮"),
            CurrencyItem("Montenegro", "EUR", "€"),
            CurrencyItem("Morocco", "MAD", "د.م."),
            CurrencyItem("Mozambique", "MZN", "MT"),
            CurrencyItem("Myanmar", "MMK", "Ks"),
            CurrencyItem("Namibia", "NAD", "$"),
            CurrencyItem("Nauru", "AUD", "$"),
            CurrencyItem("Nepal", "NPR", "₨"),
            CurrencyItem("Netherlands", "EUR", "€"),
            CurrencyItem("New Zealand", "NZD", "$"),
            CurrencyItem("Nicaragua", "NIO", "C$"),
            CurrencyItem("Niger", "XOF", "CFA"),
            CurrencyItem("Nigeria", "NGN", "₦"),
            CurrencyItem("North Macedonia", "MKD", "ден"),
            CurrencyItem("Norway", "NOK", "kr"),
            CurrencyItem("Oman", "OMR", "ر.ع."),
            CurrencyItem("Pakistan", "PKR", "₨"),
            CurrencyItem("Palau", "USD", "$"),
            CurrencyItem("Panama", "PAB", "B/."),
            CurrencyItem("Papua New Guinea", "PGK", "K"),
            CurrencyItem("Paraguay", "PYG", "₲"),
            CurrencyItem("Peru", "PEN", "S/"),
            CurrencyItem("Philippines", "PHP", "₱"),
            CurrencyItem("Poland", "PLN", "zł"),
            CurrencyItem("Portugal", "EUR", "€"),
            CurrencyItem("Qatar", "QAR", "ر.ق"),
            CurrencyItem("Romania", "RON", "lei"),
            CurrencyItem("Russia", "RUB", "₽"),
            CurrencyItem("Rwanda", "RWF", "Fr"),
            CurrencyItem("Saint Kitts and Nevis", "XCD", "$"),
            CurrencyItem("Saint Lucia", "XCD", "$"),
            CurrencyItem("Saint Vincent and the Grenadines", "XCD", "$"),
            CurrencyItem("Samoa", "WST", "T"),
            CurrencyItem("San Marino", "EUR", "€"),
            CurrencyItem("Sao Tome and Principe", "STN", "Db"),
            CurrencyItem("Saudi Arabia", "SAR", "ر.س"),
            CurrencyItem("Senegal", "XOF", "CFA"),
            CurrencyItem("Serbia", "RSD", "дин"),
            CurrencyItem("Seychelles", "SCR", "₨"),
            CurrencyItem("Sierra Leone", "SLL", "Le"),
            CurrencyItem("Singapore", "SGD", "$"),
            CurrencyItem("Slovakia", "EUR", "€"),
            CurrencyItem("Slovenia", "EUR", "€"),
            CurrencyItem("Solomon Islands", "SBD", "$"),
            CurrencyItem("Somalia", "SOS", "Sh"),
            CurrencyItem("South Africa", "ZAR", "R"),
            CurrencyItem("South Sudan", "SSP", "£"),
            CurrencyItem("Spain", "EUR", "€"),
            CurrencyItem("Sri Lanka", "LKR", "Rs"),
            CurrencyItem("Sudan", "SDG", "ج.س."),
            CurrencyItem("Suriname", "SRD", "$"),
            CurrencyItem("Sweden", "SEK", "kr"),
            CurrencyItem("Switzerland", "CHF", "Fr"),
            CurrencyItem("Syria", "SYP", "£"),
            CurrencyItem("Taiwan", "TWD", "$"),
            CurrencyItem("Tajikistan", "TJS", "SM"),
            CurrencyItem("Tanzania", "TZS", "Sh"),
            CurrencyItem("Thailand", "THB", "฿"),
            CurrencyItem("Timor-Leste", "USD", "$"),
            CurrencyItem("Togo", "XOF", "CFA"),
            CurrencyItem("Tonga", "TOP", "T$"),
            CurrencyItem("Trinidad and Tobago", "TTD", "$"),
            CurrencyItem("Tunisia", "TND", "د.ت"),
            CurrencyItem("Turkey", "TRY", "₺"),
            CurrencyItem("Turkmenistan", "TMT", "m"),
            CurrencyItem("Tuvalu", "AUD", "$"),
            CurrencyItem("Uganda", "UGX", "Sh"),
            CurrencyItem("Ukraine", "UAH", "₴"),
            CurrencyItem("United Arab Emirates", "AED", "د.إ"),
            CurrencyItem("United Kingdom", "GBP", "£"),
            CurrencyItem("United States", "USD", "$"),
            CurrencyItem("Uruguay", "UYU", "$"),
            CurrencyItem("Uzbekistan", "UZS", "so'm"),
            CurrencyItem("Vanuatu", "VUV", "Vt"),
            CurrencyItem("Vatican City", "EUR", "€"),
            CurrencyItem("Venezuela", "VES", "Bs.S"),
            CurrencyItem("Vietnam", "VND", "₫"),
            CurrencyItem("Yemen", "YER", "﷼"),
            CurrencyItem("Zambia", "ZMW", "ZK"),
            CurrencyItem("Zimbabwe", "ZWL", "$")
        )
    }

    val filteredCurrencies = if (searchText.isBlank()) {
        currencies
    } else {
        currencies.filter {
            it.country.contains(searchText, ignoreCase = true) ||
            it.code.contains(searchText, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Hader Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Select currency",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Search
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search currency") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        // List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredCurrencies) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSymbol = item.symbol }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (item.symbol == selectedSymbol),
                        onClick = { selectedSymbol = item.symbol }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.country,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.code}(${item.symbol})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Done Button
        Button(
            onClick = { onCurrencySelected(selectedSymbol) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("DONE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
