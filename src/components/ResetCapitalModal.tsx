import React, { useState } from 'react';
import { RefreshCw, AlertTriangle } from 'lucide-react';

interface ResetCapitalModalProps {
  onDismiss: () => void;
  onConfirm: (amount: number) => void;
}

export const ResetCapitalModal: React.FC<ResetCapitalModalProps> = ({
  onDismiss,
  onConfirm
}) => {
  const [selectedAmount, setSelectedAmount] = useState<number>(100000);

  const amounts = [10000, 50000, 100000, 250000];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="w-full max-w-sm bg-[#0F172A] border border-[#2B3A58] rounded-2xl p-6 shadow-2xl">
        <div className="flex items-center gap-2 mb-2">
          <RefreshCw className="w-5 h-5 text-[#00E5FF]" />
          <h3 className="text-lg font-bold text-[#F8FAFC]">Reset Paper Trading Capital</h3>
        </div>

        <p className="text-xs text-slate-300 mb-4 leading-relaxed">
          Select a simulated starting balance to test different educational strategies (e.g. small account vs institutional size):
        </p>

        {/* Options */}
        <div className="space-y-2 mb-4">
          {amounts.map((amount) => {
            const isSelected = selectedAmount === amount;
            return (
              <button
                key={amount}
                type="button"
                onClick={() => setSelectedAmount(amount)}
                className={`w-full flex items-center justify-between p-3 rounded-xl border text-sm font-semibold transition-all ${
                  isSelected
                    ? 'bg-[#00E5FF]/15 border-[#00E5FF] text-[#00E5FF]'
                    : 'bg-[#172036] border-[#2B3A58]/60 text-slate-200 hover:border-[#2B3A58]'
                }`}
              >
                <span>${amount.toLocaleString()}</span>
                {isSelected && <span className="text-xs font-bold text-[#00E5FF]">Selected</span>}
              </button>
            );
          })}
        </div>

        {/* Warning */}
        <div className="flex items-center gap-2 p-2.5 bg-[#FF3D57]/10 border border-[#FF3D57]/30 rounded-xl mb-5 text-[11px] text-[#FF3D57]">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>Warning: This will clear all existing positions and trade records.</span>
        </div>

        <div className="flex gap-2">
          <button
            type="button"
            onClick={onDismiss}
            className="flex-1 py-2.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-white bg-[#1E2942] transition-colors"
          >
            Cancel
          </button>
          <button
            type="button"
            data-testid="confirm_reset_capital_button"
            onClick={() => onConfirm(selectedAmount)}
            className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-[#00E5FF] hover:bg-[#00E5FF]/90 text-[#080D1A] transition-all shadow-md"
          >
            Reset to ${selectedAmount.toLocaleString()}
          </button>
        </div>
      </div>
    </div>
  );
};
