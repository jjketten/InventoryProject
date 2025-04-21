import { RecipeItemDTO } from './RecipeItemDTO';
import { RecipeStepDTO } from './RecipeStepDTO';

export interface RecipeDTO {
  recipeID: number | null;
  name: string;
  reference?: string;
  items: RecipeItemDTO[];
  steps: RecipeStepDTO[];
}
