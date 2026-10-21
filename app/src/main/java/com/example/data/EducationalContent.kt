package com.example.data

import com.example.model.EducationalLesson

object EducationalContent {

  val lessons = listOf(
    EducationalLesson(
      id = "lesson_1",
      title = "NASDAQ & Stock Indices 101",
      category = "Fundamentals",
      readingTime = "3 min",
      iconName = "trending_up",
      shortSummary = "Understand how the tech-heavy NASDAQ exchange works and why QQQ is the benchmark.",
      detailedBody = """
        The NASDAQ (National Association of Securities Dealers Automated Quotations) was founded in 1971 as the world's first electronic stock market. Today, it is home to over 3,000 companies, heavily tilted towards technology, software, semiconductors, and biotechnology.
        
        The NASDAQ-100 (tracked by the ETF ticker QQQ) represents the 100 largest non-financial companies listed on NASDAQ. It is market-cap weighted, meaning giants like Apple, Microsoft, and NVIDIA exert significant influence on the index's direction.
        
        When paper trading NASDAQ equities, watch QQQ as a barometer: when tech as a sector moves, individual equities frequently follow.
      """.trimIndent(),
      keyTakeaways = listOf(
        "NASDAQ is predominantly electronic and tech-focused.",
        "QQQ tracks the top 100 non-financial companies by market capitalization.",
        "Market-cap weighting means larger companies have bigger price impact."
      ),
      quizQuestion = "Why does NVIDIA or Apple have a larger impact on the NASDAQ index than smaller constituents?",
      quizOptions = listOf(
        "Because they pay higher exchange listing fees",
        "Because the index is market-capitalization weighted",
        "Because their stock prices trade faster",
        "Because only mega-caps can be traded after hours"
      ),
      correctOptionIndex = 1,
      explanation = "In a market-cap weighted index like the NASDAQ-100, companies with higher total market valuations (share price × total shares) represent a proportionally larger percentage of the index."
    ),

    EducationalLesson(
      id = "lesson_2",
      title = "Market vs. Limit Orders",
      category = "Execution",
      readingTime = "4 min",
      iconName = "swap_horiz",
      shortSummary = "Learn the critical differences between buying at current market price vs naming your price.",
      detailedBody = """
        Execution is the heartbeat of trading. The two foundational order types are Market and Limit orders:
        
        1. Market Order:
        An order to buy or sell immediately at the best available current market price.
        • Advantage: Guaranteed execution speed.
        • Risk: Price uncertainty (slippage). In fast-moving or volatile markets, the fill price may differ from the last quoted price.
        
        2. Limit Order:
        An order to buy or sell at a specified target price or better.
        • Buy Limit: Executes only at your limit price or LOWER.
        • Sell Limit: Executes only at your limit price or HIGHER.
        • Advantage: Strict price control.
        • Risk: Non-execution. If the market never touches your limit, your order remains unfilled.
      """.trimIndent(),
      keyTakeaways = listOf(
        "Market orders prioritize speed; Limit orders prioritize price.",
        "Use Market orders when entering liquid stocks where immediate fill is essential.",
        "Use Limit orders to avoid overpaying during high volatility or earnings releases."
      ),
      quizQuestion = "If Apple trades at $235.00 and you place a BUY LIMIT at $232.00, when does your order execute?",
      quizOptions = listOf(
        "Immediately at $235.00",
        "Never, because you can only buy above current price",
        "Only if the market price drops to $232.00 or lower",
        "At market close regardless of price"
      ),
      correctOptionIndex = 2,
      explanation = "A Buy Limit guarantees you will not pay more than $232.00. It stays pending until market sellers agree to trade at or below your limit price."
    ),

    EducationalLesson(
      id = "lesson_3",
      title = "The Bid-Ask Spread & Order Books",
      category = "Market Mechanics",
      readingTime = "3 min",
      iconName = "account_balance",
      shortSummary = "Discover how market makers operate and why every trade incurs a hidden spread cost.",
      detailedBody = """
        When you look at a stock quote, you will see two active prices:
        
        • Bid: The highest price a buyer in the market is currently willing to pay.
        • Ask (Offer): The lowest price a seller is currently willing to accept.
        
        The difference between the two is called the 'Spread'. When you execute a Market Buy, you buy from a seller at the Ask. When you execute a Market Sell, you sell to a buyer at the Bid.
        
        High-volume NASDAQ stocks (like AAPL or NVDA) have tight penny spreads ($0.01). Less liquid stocks have wider spreads, creating immediate friction on day trades.
      """.trimIndent(),
      keyTakeaways = listOf(
        "You always buy at the Ask and sell at the Bid with market orders.",
        "The spread is compensation for market makers providing continuous liquidity.",
        "Tighter spreads lower the hurdle for a position to become profitable."
      ),
      quizQuestion = "If NVDA is quoted at Bid $142.58 and Ask $142.60, what will you pay on an immediate Market Buy?",
      quizOptions = listOf(
        "$142.58",
        "$142.59 (midpoint)",
        "$142.60",
        "$142.62"
      ),
      correctOptionIndex = 2,
      explanation = "Market buys cross the spread to take liquidity from the lowest available seller, which is the Ask price ($142.60)."
    ),

    EducationalLesson(
      id = "lesson_4",
      title = "Reading Candlestick Charts",
      category = "Technical Analysis",
      readingTime = "4 min",
      iconName = "bar_chart",
      shortSummary = "Decode Open, High, Low, and Close price action in a single visual bar.",
      detailedBody = """
        Originating in 18th-century Japan for rice trading, Japanese Candlesticks are the standard charting method across global financial markets.
        
        Each candle summarizes price movement over a defined period (e.g. 1 minute, 1 day):
        • Real Body: The rectangle between Open and Close.
        • Green / Bullish Candle: Close was higher than Open.
        • Red / Bearish Candle: Close was lower than Open.
        • Upper Wick (Shadow): The highest price reached during the session.
        • Lower Wick: The lowest price reached during the session.
        
        Long lower wicks show aggressive buyers stepping in at lows (rejection of lower prices). Long upper wicks indicate sellers overpowering bulls at highs.
      """.trimIndent(),
      keyTakeaways = listOf(
        "Green body means Close > Open; Red body means Close < Open.",
        "Wicks demonstrate price rejection and intra-period extremes.",
        "Body length indicates the conviction and strength of the trend."
      ),
      quizQuestion = "A candle has a tiny body at the very top and a very long lower wick. What does this usually signal?",
      quizOptions = listOf(
        "Extreme selling pressure continuing down",
        "Buyers rejected lower prices and pushed price back up (Hammer)",
        "The market was closed for a holiday",
        "Trading volume was exactly zero"
      ),
      correctOptionIndex = 1,
      explanation = "A long lower shadow indicates that sellers drove prices down during the period, but buyers rejected the low and fiercely drove prices back up near the highs."
    ),

    EducationalLesson(
      id = "lesson_5",
      title = "Risk Management: The 1% Rule",
      category = "Psychology & Risk",
      readingTime = "3 min",
      iconName = "security",
      shortSummary = "The golden discipline professional traders use to protect capital and prevent blowouts.",
      detailedBody = """
        The single most common mistake in paper and live trading is oversize positioning.
        
        The 1% Rule dictates:
        Never risk more than 1% of your total account capital on any single trade.
        
        On a $100,000 simulated account, 1% risk equals $1,000 maximum loss per trade.
        
        Notice this does NOT mean only buying $1,000 of stock!
        If you buy $10,000 worth of Tesla stock with a stop loss set 10% below entry, your total dollar risk is 10% of $10,000 = $1,000 (exactly 1% of your account).
        
        Adhering to this principle ensures you can survive a streak of 10 consecutive losing trades with over 90% of your capital intact!
      """.trimIndent(),
      keyTakeaways = listOf(
        "Risk is determined by position size multiplied by stop-loss distance.",
        "Keeping maximum risk per trade under 1-2% protects mental capital and solvency.",
        "Consistency in risk management matters more than winning percentage."
      ),
      quizQuestion = "With a $100,000 account, if you buy 100 shares at $200 with a stop loss at $190, what is your dollar risk?",
      quizOptions = listOf(
        "$20,000",
        "$1,000 (1% of account)",
        "$2,000",
        "$500"
      ),
      correctOptionIndex = 1,
      explanation = "Your risk per share is $200 - $190 = $10. For 100 shares, the total dollar risk is 100 × $10 = $1,000, which is precisely 1% of the $100,000 account."
    ),

    EducationalLesson(
      id = "lesson_6",
      title = "Valuation Basics: P/E & Market Cap",
      category = "Fundamentals",
      readingTime = "3 min",
      iconName = "insights",
      shortSummary = "Why stock price alone tells you nothing about whether a stock is 'cheap' or 'expensive'.",
      detailedBody = """
        A common beginner misconception is thinking a $50 stock is 'cheaper' than a $500 stock. In reality:
        
        • Market Capitalization = Total Shares Outstanding × Current Stock Price.
        A company with 10 billion shares at $50 is worth $500 Billion! A company with 10 million shares at $500 is only worth $5 Billion.
        
        • Price-to-Earnings (P/E) Ratio:
        Calculated as Stock Price divided by Earnings Per Share (EPS).
        It shows how many dollars investors are willing to pay today for $1 of current annual earnings.
        High P/E (e.g. 50+) indicates investors expect high future revenue growth (common in NASDAQ tech/AI stocks). Low P/E (e.g. 15-20) indicates mature, cash-flowing value companies.
      """.trimIndent(),
      keyTakeaways = listOf(
        "Share price in isolation does not indicate company valuation.",
        "Market Cap gives the true total enterprise market valuation.",
        "P/E ratio reflects market growth expectations versus current profitability."
      ),
      quizQuestion = "Company A has 10M shares at $100. Company B has 100M shares at $20. Which company has the larger market cap?",
      quizOptions = listOf(
        "Company A ($1 Billion)",
        "Company B ($2 Billion)",
        "They are equal in market cap",
        "It depends on their P/E ratio"
      ),
      correctOptionIndex = 1,
      explanation = "Company A's market cap is 10M × $100 = $1B. Company B's market cap is 100M × $20 = $2B. Company B is twice as large despite having a lower nominal share price!"
    )
  )
}
