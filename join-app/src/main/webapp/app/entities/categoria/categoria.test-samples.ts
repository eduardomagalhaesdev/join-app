import { ICategoria, NewCategoria } from './categoria.model';

export const sampleWithRequiredData: ICategoria = {
  id: 34642,
  nome: 'deposit',
};

export const sampleWithPartialData: ICategoria = {
  id: 13342,
  nome: 'web',
};

export const sampleWithFullData: ICategoria = {
  id: 23485,
  nome: 'embrace jade',
};

export const sampleWithNewData: NewCategoria = {
  nome: 'Account',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
