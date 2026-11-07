<script>
  /**
   * @type {{
   *   status: string,
   *   class?: string
   * }}
   */
  let { status = '', class: className = '' } = $props();

  const normalized = $derived(status.toUpperCase());

  /** @type {Record<string, { bg: string, text: string, border: string, dot: string }>} */
  const styles = {
    APPROVED: {
      bg: 'bg-emerald-500/10',
      text: 'text-emerald-700',
      border: 'border-emerald-200/80',
      dot: 'bg-emerald-500'
    },
    PENDING: {
      bg: 'bg-amber-500/10',
      text: 'text-amber-800',
      border: 'border-amber-200/80',
      dot: 'bg-amber-500'
    },
    REJECTED: {
      bg: 'bg-rose-500/10',
      text: 'text-rose-700',
      border: 'border-rose-200/80',
      dot: 'bg-rose-500'
    },
    SAVINGS: {
      bg: 'bg-sky-500/10',
      text: 'text-sky-700',
      border: 'border-sky-200/80',
      dot: 'bg-sky-500'
    },
    CURRENT: {
      bg: 'bg-indigo-500/10',
      text: 'text-indigo-700',
      border: 'border-indigo-200/80',
      dot: 'bg-indigo-500'
    },
    ADMIN: {
      bg: 'bg-purple-500/10',
      text: 'text-purple-700',
      border: 'border-purple-200/80',
      dot: 'bg-purple-500'
    },
    CUSTOMER: {
      bg: 'bg-teal-500/10',
      text: 'text-teal-700',
      border: 'border-teal-200/80',
      dot: 'bg-teal-500'
    }
  };

  const currentStyle = $derived(
    styles[normalized] || {
      bg: 'bg-slate-100',
      text: 'text-slate-700',
      border: 'border-slate-200',
      dot: 'bg-slate-400'
    }
  );
</script>

<span
  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold tracking-wide border {currentStyle.bg} {currentStyle.text} {currentStyle.border} {className}"
>
  <span class="w-1.5 h-1.5 rounded-full {currentStyle.dot}"></span>
  <span>{status}</span>
</span>

