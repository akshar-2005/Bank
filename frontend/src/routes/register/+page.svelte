<script>
  import { goto } from '$app/navigation';
  import { api, saveSession } from '#lib/api.js';

  let fullName = $state('');
  let email = $state('');
  let password = $state('');
  let error = $state('');
  let loading = $state(false);

  /** @param {SubmitEvent} event */
  async function handleSubmit(event) {
    event.preventDefault();
    error = '';
    loading = true;
    try {
      await api('/api/auth/register', {
        method: 'POST',
        body: { fullName, email, password }
      });
      const session = await api('/api/auth/login', {
        method: 'POST',
        body: { email, password }
      });
      saveSession(session);
      goto('/dashboard');
    } catch (e) {
      error = e instanceof Error ? e.message : 'Registration failed';
    } finally {
      loading = false;
    }
  }
</script>

<div class="min-h-screen bg-slate-100 flex items-center justify-center">
  <form onsubmit={handleSubmit} class="bg-white rounded-xl shadow-md p-8 w-96 space-y-4">
    <h1 class="text-2xl font-bold text-blue-700 text-center">My Bank</h1>
    <p class="text-center text-gray-600">Create your account</p>

    {#if error}
      <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm">{error}</p>
    {/if}

    <div>
      <label class="block text-sm text-gray-700 mb-1" for="name">Full name</label>
      <input
        id="name"
        type="text"
        bind:value={fullName}
        required
        class="w-full border border-gray-300 rounded-lg px-3 py-2"
      />
    </div>

    <div>
      <label class="block text-sm text-gray-700 mb-1" for="email">Email</label>
      <input
        id="email"
        type="email"
        bind:value={email}
        required
        class="w-full border border-gray-300 rounded-lg px-3 py-2"
      />
    </div>

    <div>
      <label class="block text-sm text-gray-700 mb-1" for="password">Password (min 8 characters)</label>
      <input
        id="password"
        type="password"
        bind:value={password}
        required
        minlength="8"
        class="w-full border border-gray-300 rounded-lg px-3 py-2"
      />
    </div>

    <button
      type="submit"
      disabled={loading}
      class="w-full py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
    >
      {loading ? 'Creating account...' : 'Register'}
    </button>

    <p class="text-center text-sm text-gray-600">
      Already registered?
      <a href="/login" class="text-blue-600 hover:underline">Log in</a>
    </p>
  </form>
</div>