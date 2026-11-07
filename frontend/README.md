# sv

Everything you need to build a Svelte project, powered by [`sv`](https://github.com/sveltejs/cli).

## Creating a project

If you're seeing this, you've probably already done this step. Congrats!

```sh
# create a new project
npx sv create my-app
```

To recreate this project with the same configuration:

```sh
# recreate this project
npx sv@1.3.0 create --template minimal --no-types --add tailwindcss="plugins:none" --install npm frontend
```

## Adding features

Add features to your project with `sv add`:

```sh
npx sv add
```

For example, to add Tailwind CSS:

```sh
npx sv add tailwindcss
```

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```sh
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```sh
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.


## Frontend

A SvelteKit and Tailwind CSS web app in the `frontend` folder that uses the REST API above.

Screens: register, login, dashboard with account cards, account page (deposit, withdraw, transfer, paginated transaction history), customer loans page, and an admin page to approve or reject loans. The JWT is kept in the browser and sent with every request.

Run it (the backend must be running on port 8080):

```
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173. The backend allows this origin through a CORS setting in `SecurityConfig`.
