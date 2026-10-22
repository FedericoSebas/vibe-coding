import React from 'react';
import { useTrading } from '../context/TradingContext';
import { EDUCATIONAL_LESSONS } from '../data/EducationalContent';
import { EducationalLesson } from '../types/market';
import {
  GraduationCap,
  BookOpen,
  CheckCircle,
  ArrowRight,
  ArrowLeft,
  Lightbulb,
  HelpCircle
} from 'lucide-react';

export const AcademyScreen: React.FC = () => {
  const {
    activeLessonId,
    setActiveLessonId,
    quizAnswers,
    submitQuizAnswer
  } = useTrading();

  const lessons = EDUCATIONAL_LESSONS;

  const passedCount = lessons.filter(
    (l) => quizAnswers[l.id] === l.correctOptionIndex
  ).length;

  const activeLesson = lessons.find((l) => l.id === activeLessonId);

  if (activeLesson) {
    const selectedAnswer = quizAnswers[activeLesson.id];
    const hasAnswered = selectedAnswer !== undefined;
    const isCorrectAnswer = selectedAnswer === activeLesson.correctOptionIndex;

    return (
      <div className="flex flex-col h-full bg-[#080D1A] overflow-hidden">
        {/* Header */}
        <div className="bg-[#0F172A] border-b border-[#2B3A58]/50 px-4 py-3 flex items-center gap-3 shrink-0">
          <button
            type="button"
            data-testid="lesson_back_button"
            onClick={() => setActiveLessonId(null)}
            className="p-1.5 rounded-lg hover:bg-[#1E2942] text-slate-300 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h2 className="font-bold text-white text-sm">{activeLesson.title}</h2>
            <div className="text-xs text-slate-400">
              {activeLesson.category} • {activeLesson.readingTime}
            </div>
          </div>
        </div>

        {/* Article Body */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-24 sm:pb-8">
          {/* Main content */}
          <div className="p-5 bg-[#0F172A] border border-[#2B3A58] rounded-2xl">
            <div className="text-sm text-slate-200 leading-relaxed whitespace-pre-line">
              {activeLesson.detailedBody}
            </div>
          </div>

          {/* Key Takeaways */}
          <div className="p-5 bg-[#172036] border border-[#2B3A58] rounded-2xl">
            <div className="flex items-center gap-2 mb-3 text-[#FFD166]">
              <Lightbulb className="w-5 h-5" />
              <h3 className="font-bold text-sm">Key Takeaways</h3>
            </div>
            <ul className="space-y-2">
              {activeLesson.keyTakeaways.map((point, i) => (
                <li key={i} className="flex items-start gap-2 text-xs text-slate-200 leading-relaxed">
                  <span className="text-[#00E5FF] font-bold text-sm leading-none">•</span>
                  <span>{point}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Knowledge Check Quiz */}
          <div className="p-5 bg-[#0F172A] border border-[#2B3A58] rounded-2xl">
            <div className="flex items-center gap-2 mb-2 text-[#00E5FF]">
              <HelpCircle className="w-5 h-5" />
              <h3 className="font-bold text-sm">Knowledge Check Quiz</h3>
            </div>
            <p className="text-sm font-semibold text-white mb-4">
              {activeLesson.quizQuestion}
            </p>

            {/* Options */}
            <div className="space-y-2">
              {activeLesson.quizOptions.map((opt, idx) => {
                const isSelected = selectedAnswer === idx;
                const isCorrect = idx === activeLesson.correctOptionIndex;

                let borderCls = 'border-[#2B3A58]';
                let bgCls = 'bg-[#1E2942]/60 hover:bg-[#1E2942]';

                if (hasAnswered) {
                  if (isCorrect) {
                    borderCls = 'border-[#00E676]';
                    bgCls = 'bg-[#00E676]/15';
                  } else if (isSelected) {
                    borderCls = 'border-[#FF3D57]';
                    bgCls = 'bg-[#FF3D57]/15';
                  }
                }

                return (
                  <button
                    key={idx}
                    type="button"
                    data-testid={`quiz_option_${idx}`}
                    onClick={() => submitQuizAnswer(activeLesson.id, idx)}
                    className={`w-full flex items-center text-left p-3 rounded-xl border text-xs transition-all ${borderCls} ${bgCls}`}
                  >
                    <span
                      className={`font-bold w-6 shrink-0 ${
                        hasAnswered && isCorrect ? 'text-[#00E676]' : 'text-slate-400'
                      }`}
                    >
                      {String.fromCharCode(65 + idx)}.
                    </span>
                    <span className="text-white font-medium">{opt}</span>
                  </button>
                );
              })}
            </div>

            {/* Explanation */}
            {hasAnswered && (
              <div
                className={`mt-4 p-3.5 rounded-xl border text-xs leading-relaxed ${
                  isCorrectAnswer
                    ? 'bg-[#00E676]/10 border-[#00E676]/30 text-slate-200'
                    : 'bg-[#172036] border-[#2B3A58] text-slate-300'
                }`}
              >
                <div
                  className={`font-bold text-sm mb-1 ${
                    isCorrectAnswer ? 'text-[#00E676]' : 'text-[#FF3D57]'
                  }`}
                >
                  {isCorrectAnswer ? '✓ Correct!' : '✗ Not quite'}
                </div>
                <div>{activeLesson.explanation}</div>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-[#080D1A] overflow-hidden">
      {/* Header */}
      <div className="bg-[#0F172A] border-b border-[#2B3A58]/50 px-4 py-3 flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-lg font-bold text-[#F8FAFC]">Trading Academy</h1>
          <p className="text-xs text-slate-400">Educational Guide & Knowledge Checks</p>
        </div>

        <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#7C4DFF]/20 text-[#7C4DFF] text-xs font-bold border border-[#7C4DFF]/30">
          <GraduationCap className="w-4 h-4" />
          <span>{passedCount}/{lessons.length} Passed</span>
        </div>
      </div>

      {/* Lessons List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3 pb-24 sm:pb-6">
        {lessons.map((lesson) => {
          const isPassed = quizAnswers[lesson.id] === lesson.correctOptionIndex;

          return (
            <div
              key={lesson.id}
              data-testid={`lesson_card_${lesson.id}`}
              onClick={() => setActiveLessonId(lesson.id)}
              className="p-4 bg-[#0F172A] hover:bg-[#172036] border border-[#2B3A58]/70 hover:border-[#00E5FF]/40 rounded-xl cursor-pointer transition-all flex items-center justify-between gap-3"
            >
              <div className="flex items-start gap-3 flex-1 min-w-0">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 mt-0.5 ${
                    isPassed ? 'bg-[#00E676]/20 text-[#00E676]' : 'bg-[#00E5FF]/15 text-[#00E5FF]'
                  }`}
                >
                  {isPassed ? <CheckCircle className="w-5 h-5" /> : <BookOpen className="w-5 h-5" />}
                </div>

                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="px-2 py-0.5 rounded bg-[#1E2942] text-[#FFD166] text-[10px] font-semibold">
                      {lesson.category}
                    </span>
                    <span className="text-[11px] text-slate-500">• {lesson.readingTime}</span>
                  </div>

                  <h3 className="font-bold text-white text-sm mb-1">{lesson.title}</h3>
                  <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                    {lesson.shortSummary}
                  </p>
                </div>
              </div>

              <ArrowRight className="w-4 h-4 text-slate-500 shrink-0" />
            </div>
          );
        })}
      </div>
    </div>
  );
};
