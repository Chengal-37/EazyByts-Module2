// js/dashboard.js
import { API_BASE_URL, showAlert } from './utils.js';
import { logout } from './app.js';

// --- Modal Elements and Bootstrap Instances ---
const buyStockModalElement = document.getElementById('buyStockModal');
const buyStockModal = new bootstrap.Modal(buyStockModalElement);
const buyStockSymbolSpan = document.getElementById('buyStockSymbol');
const buyQuantityInput = document.getElementById('buyQuantityInput');
const confirmBuyButton = document.getElementById('confirmBuyButton');

const sellStockModalElement = document.getElementById('sellStockModal');
const sellStockModal = new bootstrap.Modal(sellStockModalElement);
const sellStockSymbolSpan = document.getElementById('sellStockSymbol');
const sellQuantityInput = document.getElementById('sellQuantityInput');
const maxSellQuantitySpan = document.getElementById('maxSellQuantity');
const confirmSellButton = document.getElementById('confirmSellButton');

let currentBuySymbol = '';
let currentSellSymbol = '';
let currentMaxSellShares = 0;

// --- Variables for Available Stocks Pagination/Search ---
let allAvailableStocks = []; // Stores the full list of stocks from the backend
let filteredAvailableStocks = []; // Stores stocks after applying search filter
let currentStocksPage = 1;
let stocksPerPage = 10; // Default items per page

// --- Pagination Elements for Available Stocks ---
const stocksPrevPageBtn = document.getElementById('stocksPrevPage');
const stocksNextPageBtn = document.getElementById('stocksNextPage');
const stocksPageInfoSpan = document.getElementById('stocksPageInfo');
const stocksPerPageSelect = document.getElementById('stocksPerPage');
const stockSearchInput = document.getElementById('stockSearchInput');
const stockSearchButton = document.getElementById('stockSearchButton');
const clearStockSearchButton = document.getElementById('clearStockSearchButton');

// --- NEW: Variables for Transaction History Pagination/Search/Filter ---
let allTransactions = []; // Stores the full list of transactions
let filteredTransactions = []; // Stores transactions after applying search/filter
let currentTransactionsPage = 1;
let transactionsPerPage = 10; // Default items per page

// --- NEW: Elements for Transaction History ---
const transactionSearchInput = document.getElementById('transactionSearchInput');
const transactionSearchButton = document.getElementById('transactionSearchButton');
const clearTransactionSearchButton = document.getElementById('clearTransactionSearchButton');
const transactionTypeFilterSelect = document.getElementById('transactionTypeFilter');
const transactionsPrevPageBtn = document.getElementById('transactionsPrevPage');
const transactionsNextPageBtn = document.getElementById('transactionsNextPage');
const transactionsPageInfoSpan = document.getElementById('transactionsPageInfo');
const transactionsPerPageSelect = document.getElementById('transactionsPerPage');

// --- NEW: Element for Market Stocks Display ---
const marketStocksContainer = document.getElementById('marketStocksContainer');


document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('authToken');
    if (!token) {
        showAlert('You are not logged in. Please log in to access the dashboard.', 'warning');
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);
        return;
    }

    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', () => {
            logout();
        });
    }

    fetchUserProfile(token);
    fetchMarketOverview(token);
    fetchUserPortfolio(token);
    fetchAvailableStocks(token); // Initial fetch now populates `allAvailableStocks`
    fetchTransactionHistory(token); // Initial fetch now populates `allTransactions`

    const profileUpdateForm = document.getElementById('profileUpdateForm');
    if (profileUpdateForm) {
        profileUpdateForm.addEventListener('submit', (event) => handleProfileUpdate(event, token));
    }

    // --- Stock Search Button Listener ---
    if (stockSearchButton) {
        stockSearchButton.addEventListener('click', () => {
            applyStockSearchFilter();
        });
    }

    if (stockSearchInput) {
        stockSearchInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                applyStockSearchFilter();
            }
        });
    }

    // --- Clear Stock Search Button Listener ---
    if (clearStockSearchButton) {
        clearStockSearchButton.addEventListener('click', () => {
            stockSearchInput.value = ''; // Clear the input field
            applyStockSearchFilter(); // Re-apply filter, which will now show all stocks
        });
    }

    // --- Available Stocks Pagination Event Listeners ---
    if (stocksPrevPageBtn) {
        stocksPrevPageBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (currentStocksPage > 1) {
                currentStocksPage--;
                renderAvailableStocksTable(filteredAvailableStocks, currentStocksPage, stocksPerPage);
            }
        });
    }

    if (stocksNextPageBtn) {
        stocksNextPageBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const totalPages = Math.ceil(filteredAvailableStocks.length / stocksPerPage);
            if (currentStocksPage < totalPages) {
                currentStocksPage++;
                renderAvailableStocksTable(filteredAvailableStocks, currentStocksPage, stocksPerPage);
            }
        });
    }

    if (stocksPerPageSelect) {
        stocksPerPageSelect.addEventListener('change', (e) => {
            stocksPerPage = parseInt(e.target.value, 10);
            currentStocksPage = 1;
            renderAvailableStocksTable(filteredAvailableStocks, currentStocksPage, stocksPerPage);
        });
    }

    // --- NEW: Transaction History Search, Filter, and Pagination Event Listeners ---
    if (transactionSearchButton) {
        transactionSearchButton.addEventListener('click', () => {
            applyTransactionFiltersAndSearch();
        });
    }

    if (transactionSearchInput) {
        transactionSearchInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                applyTransactionFiltersAndSearch();
            }
        });
    }

    if (clearTransactionSearchButton) {
        clearTransactionSearchButton.addEventListener('click', () => {
            transactionSearchInput.value = '';
            applyTransactionFiltersAndSearch(); // Re-apply filters
        });
    }

    if (transactionTypeFilterSelect) {
        transactionTypeFilterSelect.addEventListener('change', () => {
            applyTransactionFiltersAndSearch();
        });
    }

    if (transactionsPrevPageBtn) {
        transactionsPrevPageBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (currentTransactionsPage > 1) {
                currentTransactionsPage--;
                renderTransactionHistoryTable(filteredTransactions, currentTransactionsPage, transactionsPerPage);
            }
        });
    }

    if (transactionsNextPageBtn) {
        transactionsNextPageBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const totalPages = Math.ceil(filteredTransactions.length / transactionsPerPage);
            if (currentTransactionsPage < totalPages) {
                currentTransactionsPage++;
                renderTransactionHistoryTable(filteredTransactions, currentTransactionsPage, transactionsPerPage);
            }
        });
    }

    if (transactionsPerPageSelect) {
        transactionsPerPageSelect.addEventListener('change', (e) => {
            transactionsPerPage = parseInt(e.target.value, 10);
            currentTransactionsPage = 1;
            renderTransactionHistoryTable(filteredTransactions, currentTransactionsPage, transactionsPerPage);
        });
    }


    confirmBuyButton.addEventListener('click', () => {
        const token = localStorage.getItem('authToken');
        const quantity = parseInt(buyQuantityInput.value, 10);

        if (isNaN(quantity) || quantity <= 0) {
            showAlert('Please enter a valid positive quantity.', 'warning');
            return;
        }

        buyStockModal.hide();
        executeBuyTrade(currentBuySymbol, quantity, token);
    });

    confirmSellButton.addEventListener('click', () => {
        const token = localStorage.getItem('authToken');
        const quantity = parseInt(sellQuantityInput.value, 10);

        if (isNaN(quantity) || quantity <= 0) {
            showAlert('Please enter a valid positive quantity.', 'warning');
            return;
        }
        if (quantity > currentMaxSellShares) {
            showAlert(`You can only sell up to ${currentMaxSellShares} shares.`, 'warning');
            return;
        }

        sellStockModal.hide();
        executeSellTrade(currentSellSymbol, quantity, token);
    });
});

/**
 * Fetches the user's profile data from the backend.
 * @param {string} token The authentication token.
 */
async function fetchUserProfile(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/users/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const user = await response.json();
            displayUserProfile(user);
        } else if (response.status === 401) {
            showAlert('Session expired. Please log in again.', 'warning');
            logout();
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || 'Failed to fetch user profile.', 'danger');
        }
    } catch (error) {
        console.error('Error fetching user profile:', error);
        showAlert('An error occurred while fetching profile data.', 'danger');
    }
}

/**
 * Displays the fetched user profile data on the dashboard.
 * @param {object} user The user object.
 */
function displayUserProfile(user) {
    document.getElementById('profileUsernameWelcome').textContent = user.username;
    document.getElementById('profileUsername').textContent = user.username;
    document.getElementById('profileEmail').textContent = user.email;

    document.getElementById('profileFirstName').textContent = user.firstName || 'N/A';
    document.getElementById('profileLastName').textContent = user.lastName || 'N/A';
    document.getElementById('accountBalance').textContent = `₹${(user.accountBalance || 0).toFixed(2)}`;

    document.getElementById('updateFirstName').value = user.firstName || '';
    document.getElementById('updateLastName').value = user.lastName || '';
}

/**
 * Handles the user profile update process.
 * @param {Event} event The submit event from the profile update form.
 * @param {string} token The authentication token.
 */
async function handleProfileUpdate(event, token) {
    event.preventDefault();

    const firstName = document.getElementById('updateFirstName').value.trim();
    const lastName = document.getElementById('updateLastName').value.trim();
    const password = document.getElementById('updatePassword').value.trim();

    if (!firstName || !lastName) {
        showAlert('First Name and Last Name are required.', 'warning');
        return;
    }
    if (firstName.length < 2) {
        showAlert('First Name must be at least 2 characters long.', 'warning');
        return;
    }
    if (lastName.length < 2) {
        showAlert('Last Name must be at least 2 characters long.', 'warning');
        return;
    }
    if (password && password.length < 6) {
        showAlert('New password must be at least 6 characters long if provided.', 'warning');
        return;
    }

    const updateData = { firstName, lastName };
    if (password) {
        updateData.newPassword = password;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/users/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(updateData)
        });

        if (response.ok) {
            const updatedUser = await response.json();
            displayUserProfile(updatedUser);
            showAlert('Profile updated successfully!', 'success');
            document.getElementById('updatePassword').value = '';
        } else if (response.status === 401) {
            showAlert('Session expired. Please log in again to update your profile.', 'warning');
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || 'Failed to update profile.', 'danger');
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        showAlert('An error occurred while updating profile.', 'danger');
    }
}


/**
 * Fetches and displays market overview data (e.g., Nifty, Sensex).
 * @param {string} token The authentication token.
 */
async function fetchMarketOverview(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/market-data`, { // Placeholder endpoint
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('niftyIndex').textContent = data.nifty || 'N/A';
            document.getElementById('sensexIndex').textContent = data.sensex || 'N/A';
        } else {
            showAlert('Failed to load market overview data.', 'danger');
        }
    } catch (error) {
        console.error('Error fetching market overview:', error);
        showAlert('An error occurred while fetching market overview.', 'danger');
    }
}

/**
 * Fetches and displays the user's stock portfolio.
 * @param {string} token The authentication token.
 */
async function fetchUserPortfolio(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/portfolio`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const tableBody = document.getElementById('userPortfolioTable').querySelector('tbody');
        tableBody.innerHTML = ''; // Clear existing rows

        if (response.ok) {
            const portfolioResponse = await response.json();
            let totalPortfolioValue = 0;

            const holdings = portfolioResponse.holdings;

            if (holdings && holdings.length > 0) {
                holdings.forEach(item => {
                    const gainLossPercent = (item.currentPrice - item.avgPrice) / item.avgPrice * 100;
                    totalPortfolioValue += item.totalValue;

                    const row = `
                        <tr>
                            <td>${item.symbol}</td>
                            <td>${item.companyName}</td>
                            <td>${item.shares}</td>
                            <td>₹${item.avgPrice.toFixed(2)}</td>
                            <td>₹${item.currentPrice.toFixed(2)}</td>
                            <td>₹${item.totalValue.toFixed(2)}</td>
                            <td class="${gainLossPercent >= 0 ? 'text-success' : 'text-danger'}">${gainLossPercent.toFixed(2)}%</td>
                            <td><button class="btn btn-sm btn-danger sell-btn" data-symbol="${item.symbol}" data-shares="${item.shares}">Sell</button></td>
                        </tr>
                    `;
                    tableBody.insertAdjacentHTML('beforeend', row);
                });

                document.querySelectorAll('.sell-btn').forEach(button => {
                    button.addEventListener('click', (e) => {
                        const symbol = e.target.dataset.symbol;
                        const shares = parseInt(e.target.dataset.shares, 10);
                        const currentToken = localStorage.getItem('authToken');
                        if (currentToken) {
                            handleSellStock(symbol, shares, currentToken);
                        } else {
                            showAlert('You must be logged in to sell stocks.', 'danger');
                            logout();
                        }
                    });
                });

            } else {
                tableBody.innerHTML = `<tr><td colspan="8" class="text-center">Your portfolio is empty.</td></tr>`;
            }
            document.getElementById('totalPortfolioValue').textContent = totalPortfolioValue.toFixed(2);
        } else {
            tableBody.innerHTML = `<tr><td colspan="8" class="text-center">Failed to load portfolio.</td></tr>`;
            showAlert('Failed to load portfolio data.', 'danger');
        }
    } catch (error) {
        console.error('Error fetching user portfolio:', error);
        showAlert('An error occurred while fetching portfolio.', 'danger');
    }
}

/**
 * Fetches and stores all available stocks. Then renders the first page and popular stocks in market overview.
 * @param {string} token The authentication token.
 */
async function fetchAvailableStocks(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/stocks`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            allAvailableStocks = await response.json();
            filteredAvailableStocks = [...allAvailableStocks]; // Initialize filtered with all stocks
            currentStocksPage = 1; // Reset to first page
            renderAvailableStocksTable(filteredAvailableStocks, currentStocksPage, stocksPerPage);
            renderMarketStocks(allAvailableStocks.slice(0, 5)); // Render top 5 stocks in market overview
        } else {
            const tableBody = document.getElementById('availableStocksTable').querySelector('tbody');
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center">Failed to load available stocks.</td></tr>`;
            showAlert('Failed to load available stocks data.', 'danger');
            marketStocksContainer.innerHTML = `<p class="text-center text-muted">Failed to load popular stocks.</p>`;
        }
    } catch (error) {
        console.error('Error fetching available stocks:', error);
        showAlert('An error occurred while fetching available stocks.', 'danger');
        const tableBody = document.getElementById('availableStocksTable').querySelector('tbody');
        tableBody.innerHTML = `<tr><td colspan="5" class="text-center">An error occurred while loading stocks.</td></tr>`;
        marketStocksContainer.innerHTML = `<p class="text-center text-muted">An error occurred while loading popular stocks.</p>`;
    }
}

/**
 * Renders the Available Stocks table with pagination and search results.
 * @param {Array} stocksToDisplay The array of stocks to render (can be all or filtered).
 * @param {number} page The current page number.
 * @param {number} itemsPerPage The number of items to show per page.
 */
function renderAvailableStocksTable(stocksToDisplay, page, itemsPerPage) {
    const tableBody = document.getElementById('availableStocksTable').querySelector('tbody');
    tableBody.innerHTML = ''; // Clear previous data

    const startIndex = (page - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedStocks = stocksToDisplay.slice(startIndex, endIndex);

    if (paginatedStocks && paginatedStocks.length > 0) {
        paginatedStocks.forEach(stock => {
            const changePercent = ((stock.currentPrice - stock.previousClose) / stock.previousClose * 100) || 0;
            const row = `
                <tr>
                    <td>${stock.symbol}</td>
                    <td>${stock.companyName}</td>
                    <td>₹${stock.currentPrice.toFixed(2)}</td>
                    <td class="${changePercent >= 0 ? 'text-success' : 'text-danger'}">${changePercent.toFixed(2)}%</td>
                    <td><button class="btn btn-sm btn-info buy-btn" data-symbol="${stock.symbol}">Buy</button></td>
                </tr>
            `;
            tableBody.insertAdjacentHTML('beforeend', row);
        });

        // Re-attach event listeners for Buy buttons
        document.querySelectorAll('.buy-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const symbol = e.target.dataset.symbol;
                const currentToken = localStorage.getItem('authToken');
                if (currentToken) {
                    handleBuyStock(symbol, currentToken);
                } else {
                    showAlert('You must be logged in to buy stocks.', 'danger');
                    logout();
                }
            });
        });

    } else {
        tableBody.innerHTML = `<tr><td colspan="5" class="text-center">No stocks found matching your criteria.</td></tr>`;
    }

    // Update pagination controls
    updateStocksPaginationControls(stocksToDisplay.length, page, itemsPerPage);
}

/**
 * NEW: Renders a subset of stocks into the Market Overview section.
 * @param {Array} stocks A subset of stock data to display.
 */
function renderMarketStocks(stocks) {
    if (!marketStocksContainer) return; // Ensure element exists

    marketStocksContainer.innerHTML = ''; // Clear previous content

    if (stocks && stocks.length > 0) {
        const ul = document.createElement('ul');
        ul.classList.add('list-unstyled', 'mb-0'); // Bootstrap classes

        stocks.forEach(stock => {
            const changePercent = ((stock.currentPrice - stock.previousClose) / stock.previousClose * 100) || 0;
            const li = document.createElement('li');
            li.classList.add('market-stock-item');
            li.innerHTML = `
                <span>${stock.symbol}</span>
                <span>₹${stock.currentPrice.toFixed(2)} <span class="${changePercent >= 0 ? 'text-success' : 'text-danger'}">(${changePercent.toFixed(2)}%)</span></span>
            `;
            ul.appendChild(li);
        });
        marketStocksContainer.appendChild(ul);
    } else {
        marketStocksContainer.innerHTML = `<p class="text-center text-muted">No popular stock data available.</p>`;
    }
}


/**
 * Applies the search filter to allAvailableStocks and re-renders the table.
 */
function applyStockSearchFilter() {
    const query = stockSearchInput.value.trim().toLowerCase();

    if (query) {
        filteredAvailableStocks = allAvailableStocks.filter(stock =>
            stock.symbol.toLowerCase().includes(query) ||
            stock.companyName.toLowerCase().includes(query)
        );
    } else {
        filteredAvailableStocks = [...allAvailableStocks]; // Show all if search is empty
    }

    currentStocksPage = 1; // Always reset to the first page after a new search
    renderAvailableStocksTable(filteredAvailableStocks, currentStocksPage, stocksPerPage);
}

/**
 * Updates the pagination buttons and page info for Available Stocks.
 * @param {number} totalItems The total number of items after filtering.
 * @param {number} currentPage The current page number.
 * @param {number} itemsPerPage The number of items per page.
 */
function updateStocksPaginationControls(totalItems, currentPage, itemsPerPage) {
    const totalPages = Math.ceil(totalItems / itemsPerPage);

    stocksPageInfoSpan.textContent = `Page ${currentPage} of ${totalPages || 1}`;

    if (currentPage === 1) {
        stocksPrevPageBtn.classList.add('disabled');
    } else {
        stocksPrevPageBtn.classList.remove('disabled');
    }

    if (currentPage === totalPages || totalPages === 0) {
        stocksNextPageBtn.classList.add('disabled');
    } else {
        stocksNextPageBtn.classList.remove('disabled');
    }
}


/**
 * Displays the buy stock modal.
 * @param {string} symbol The symbol of the stock to buy.
 * @param {string} token The authentication token.
 */
async function handleBuyStock(symbol, token) {
    currentBuySymbol = symbol;
    buyStockSymbolSpan.textContent = symbol;
    buyQuantityInput.value = 1;
    buyStockModal.show();
}

/**
 * Executes the buy trade after modal confirmation.
 * @param {string} symbol The symbol of the stock to buy.
 * @param {number} quantity The quantity to buy.
 * @param {string} token The authentication token.
 */
async function executeBuyTrade(symbol, quantity, token) {
    try {
        const response = await fetch(`${API_BASE_URL}/trades/buy`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                stockSymbol: symbol,
                quantity: quantity
            })
        });

        if (response.ok) {
            const result = await response.json();
            showAlert(result.message || `Successfully bought ${quantity} shares of ${symbol}!`, 'success');
            fetchUserPortfolio(token);
            fetchUserProfile(token);
            fetchAvailableStocks(token); // Refresh stocks as prices might change
            fetchTransactionHistory(token); // This will now trigger the new logic
        } else if (response.status === 401) {
            showAlert('Session expired. Please log in again to buy stocks.', 'warning');
            logout();
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || `Failed to buy ${symbol}.`, 'danger');
        }
    } catch (error) {
        console.error('Error buying stock:', error);
        showAlert('An error occurred while trying to buy the stock.', 'danger');
    }
}

/**
 * Displays the sell stock modal.
 * @param {string} symbol The symbol of the stock to sell.
 * @param {number} sharesOwned The number of shares the user currently owns.
 * @param {string} token The authentication token.
 */
async function handleSellStock(symbol, sharesOwned, token) {
    currentSellSymbol = symbol;
    currentMaxSellShares = sharesOwned;
    sellStockSymbolSpan.textContent = symbol;
    sellQuantityInput.value = 1;
    sellQuantityInput.setAttribute('max', sharesOwned);
    maxSellQuantitySpan.textContent = `You own: ${sharesOwned} shares`;
    sellStockModal.show();
}

/**
 * Executes the sell trade after modal confirmation.
 * @param {string} symbol The symbol of the stock to sell.
 * @param {number} quantity The quantity to sell.
 * @param {string} token The authentication token.
 */
async function executeSellTrade(symbol, quantity, token) {
    try {
        const response = await fetch(`${API_BASE_URL}/trades/sell`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                stockSymbol: symbol,
                quantity: quantity
            })
        });

        if (response.ok) {
            const result = await response.json();
            showAlert(result.message || `Successfully sold ${quantity} shares of ${symbol}!`, 'success');
            fetchUserPortfolio(token);
            fetchUserProfile(token);
            fetchAvailableStocks(token); // Refresh stocks as prices might change
            fetchTransactionHistory(token); // This will now trigger the new logic
        } else if (response.status === 401) {
            showAlert('Session expired. Please log in again to sell stocks.', 'warning');
            logout();
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || `Failed to sell ${symbol}.`, 'danger');
        }
    } catch (error) {
        console.error('Error selling stock:', error);
        showAlert('An error occurred while trying to sell the stock.', 'danger');
    }
}


/**
 * Fetches and stores all transaction history. Then applies filters and renders the first page.
 * @param {string} token The authentication token.
 */
async function fetchTransactionHistory(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/trades/history`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            allTransactions = await response.json(); // Store all transactions
            applyTransactionFiltersAndSearch(); // Apply initial filters and render
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || 'Failed to load transaction history.', 'danger');
            const tableBody = document.getElementById('transactionHistoryTable').querySelector('tbody');
            tableBody.innerHTML = `<tr><td colspan="7" class="text-center">Failed to load transaction history.</td></tr>`;
        }
    } catch (error) {
        console.error('Error fetching transaction history:', error);
        showAlert('An error occurred while fetching transaction history.', 'danger');
        const tableBody = document.getElementById('transactionHistoryTable').querySelector('tbody');
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center">An error occurred while loading transactions.</td></tr>`;
    }
}

/**
 * Renders the Transaction History table with pagination, search, and filter results.
 * @param {Array} transactionsToDisplay The array of transactions to render (can be all or filtered).
 * @param {number} page The current page number.
 * @param {number} itemsPerPage The number of items to show per page.
 */
function renderTransactionHistoryTable(transactionsToDisplay, page, itemsPerPage) {
    const tableBody = document.getElementById('transactionHistoryTable').querySelector('tbody');
    tableBody.innerHTML = ''; // Clear previous data

    const startIndex = (page - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedTransactions = transactionsToDisplay.slice(startIndex, endIndex);

    if (paginatedTransactions && paginatedTransactions.length > 0) {
        paginatedTransactions.forEach(t => {
            const transactionDate = new Date(t.timestamp);
            const formattedDate = transactionDate.toLocaleString('en-IN', {
                year: 'numeric',
                month: 'short',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: true
            });

            const row = `
                <tr>
                    <td>${formattedDate}</td>
                    <td>${t.stockSymbol}</td>
                    <td>${t.stockName}</td>
                    <td><span class="badge bg-${t.transactionType === 'BUY' ? 'success' : 'danger'}">${t.transactionType}</span></td>
                    <td>${t.quantity}</td>
                    <td>₹${t.priceAtTrade.toFixed(2)}</td>
                    <td>₹${t.totalAmount.toFixed(2)}</td>
                </tr>
            `;
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    } else {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center">No transactions found matching your criteria.</td></tr>`;
    }

    // Update pagination controls
    updateTransactionPaginationControls(transactionsToDisplay.length, page, itemsPerPage);
}

/**
 * Applies search and type filters to allTransactions and re-renders the table.
 */
function applyTransactionFiltersAndSearch() {
    const searchQuery = transactionSearchInput.value.trim().toLowerCase();
    const typeFilter = transactionTypeFilterSelect.value; // ALL, BUY, or SELL

    let tempFilteredTransactions = [...allTransactions];

    // Apply search filter
    if (searchQuery) {
        tempFilteredTransactions = tempFilteredTransactions.filter(t =>
            t.stockSymbol.toLowerCase().includes(searchQuery) ||
            t.stockName.toLowerCase().includes(searchQuery)
        );
    }

    // Apply type filter
    if (typeFilter !== 'ALL') {
        tempFilteredTransactions = tempFilteredTransactions.filter(t =>
            t.transactionType === typeFilter
        );
    }

    filteredTransactions = tempFilteredTransactions; // Update the globally filtered array
    currentTransactionsPage = 1; // Reset to the first page after applying new filters
    renderTransactionHistoryTable(filteredTransactions, currentTransactionsPage, transactionsPerPage);
}

/**
 * Updates the pagination buttons and page info for Transaction History.
 * @param {number} totalItems The total number of items after filtering.
 * @param {number} currentPage The current page number.
 * @param {number} itemsPerPage The number of items per page.
 */
function updateTransactionPaginationControls(totalItems, currentPage, itemsPerPage) {
    const totalPages = Math.ceil(totalItems / itemsPerPage);

    transactionsPageInfoSpan.textContent = `Page ${currentPage} of ${totalPages || 1}`;

    if (currentPage === 1) {
        transactionsPrevPageBtn.classList.add('disabled');
    } else {
        transactionsPrevPageBtn.classList.remove('disabled');
    }

    if (currentPage === totalPages || totalPages === 0) {
        transactionsNextPageBtn.classList.add('disabled');
    } else {
        transactionsNextPageBtn.classList.remove('disabled');
    }
}