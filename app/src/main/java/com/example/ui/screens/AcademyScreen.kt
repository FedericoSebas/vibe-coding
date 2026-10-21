package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EducationalLesson
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBg
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDarkest
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AcademyScreen(
  lessons: List<EducationalLesson>,
  activeLesson: EducationalLesson?,
  quizAnswers: Map<String, Int>,
  onSelectLesson: (EducationalLesson?) -> Unit,
  onAnswerQuiz: (lessonId: String, selectedIndex: Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (activeLesson != null) {
    LessonDetailView(
      lesson = activeLesson,
      selectedAnswer = quizAnswers[activeLesson.id],
      onBack = { onSelectLesson(null) },
      onSelectAnswer = { idx -> onAnswerQuiz(activeLesson.id, idx) }
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NavyDarkest)
  ) {
    // Top Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(NavyDark)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Trading Academy",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Text(
            text = "Educational Guide & Knowledge Checks",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Surface(
          color = PurpleAccent.copy(alpha = 0.2f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.School, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            val completedCount = quizAnswers.count { (id, answer) ->
              val l = lessons.find { it.id == id }
              l != null && l.correctOptionIndex == answer
            }
            Text(
              text = "$completedCount/${lessons.size} Passed",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PurpleAccent
            )
          }
        }
      }
    }

    // Lessons List
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(lessons, key = { it.id }) { lesson ->
        val answer = quizAnswers[lesson.id]
        val isPassed = answer != null && answer == lesson.correctOptionIndex

        LessonCardItem(
          lesson = lesson,
          isPassed = isPassed,
          onClick = { onSelectLesson(lesson) }
        )
      }
    }
  }
}

@Composable
fun LessonCardItem(
  lesson: EducationalLesson,
  isPassed: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = NavyDark),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("lesson_card_${lesson.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(if (isPassed) ProfitGreenBg else CyanNeon.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.MenuBook,
          contentDescription = null,
          tint = if (isPassed) ProfitGreen else CyanNeon,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = NavySurfaceVariant,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = lesson.category,
              fontSize = 10.sp,
              color = GoldAccent,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "• ${lesson.readingTime}", fontSize = 11.sp, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = lesson.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = lesson.shortSummary,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Read",
        tint = TextMuted,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
fun LessonDetailView(
  lesson: EducationalLesson,
  selectedAnswer: Int?,
  onBack: () -> Unit,
  onSelectAnswer: (Int) -> Unit
) {
  BackHandler { onBack() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(NavyDarkest)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(NavyDark)
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("lesson_back_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
      }
      Column {
        Text(
          text = lesson.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Text(
          text = "${lesson.category} • ${lesson.readingTime}",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted
        )
      }
    }

    // Scrollable Article Body
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Main Body Card
      Card(
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = lesson.detailedBody,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            lineHeight = 22.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Key Takeaways Card
      Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Key Takeaways",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = GoldAccent
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          lesson.keyTakeaways.forEach { point ->
            Row(
              modifier = Modifier.padding(vertical = 4.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text("•", color = CyanNeon, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
              Text(point, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Interactive Knowledge Quiz
      Card(
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Knowledge Check Quiz",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = CyanNeon
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = lesson.quizQuestion,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(12.dp))

          lesson.quizOptions.forEachIndexed { index, optionText ->
            val isSelected = selectedAnswer == index
            val isCorrect = index == lesson.correctOptionIndex
            val hasAnswered = selectedAnswer != null

            val borderColor = when {
              hasAnswered && isCorrect -> ProfitGreen
              hasAnswered && isSelected && !isCorrect -> LossRed
              isSelected -> CyanNeon
              else -> NavyBorder
            }

            val bgColor = when {
              hasAnswered && isCorrect -> ProfitGreenBg
              hasAnswered && isSelected && !isCorrect -> LossRedBg
              isSelected -> CyanNeon.copy(alpha = 0.12f)
              else -> NavySurfaceVariant
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .background(bgColor)
                .clickable { onSelectAnswer(index) }
                .padding(12.dp)
                .testTag("quiz_option_$index")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${'A' + index}.",
                  fontWeight = FontWeight.Bold,
                  color = if (hasAnswered && isCorrect) ProfitGreen else TextSecondary,
                  modifier = Modifier.width(26.dp)
                )
                Text(
                  text = optionText,
                  fontSize = 13.sp,
                  color = TextPrimary,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }

          // Show explanation after answering
          if (selectedAnswer != null) {
            val answeredCorrectly = selectedAnswer == lesson.correctOptionIndex
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              color = if (answeredCorrectly) ProfitGreenBg else NavySurface,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = if (answeredCorrectly) "✓ Correct!" else "✗ Not quite",
                  fontWeight = FontWeight.Bold,
                  color = if (answeredCorrectly) ProfitGreen else LossRed,
                  fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = lesson.explanation,
                  fontSize = 12.sp,
                  color = TextSecondary,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}
